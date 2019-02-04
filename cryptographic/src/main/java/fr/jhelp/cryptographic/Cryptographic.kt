package fr.jhelp.cryptographic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.security.KeyPairGeneratorSpec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.RSAKeyGenParameterSpec
import java.util.GregorianCalendar
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.security.auth.x500.X500Principal

const val KEY_ALIAS = "JHelp"
const val AndroidKeyStore = "AndroidKeyStore"
const val RSA_MODE = "RSA/ECB/PKCS1Padding"
const val KEY_SIZE = 512
const val RSA = "RSA"

private val HEXA = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

private fun toHexa(byte: Byte): String
{
    val value = byte.toInt() and 0xFF
    return String(charArrayOf(HEXA[value shr 4], HEXA[value and 0xF]))
}

private fun toHexa(array: ByteArray): String
{
    val string = StringBuilder()
    array.forEach { string.append(toHexa(it)) }
    return string.toString()
}

private fun fromHexa(char1: Char, char2: Char): Byte
{
    val index1 = HEXA.indexOf(char1)
    val index2 = HEXA.indexOf(char2)
    return ((index1 shl 4) or index2).toByte()
}

private fun fromHexa(hexa: String): ByteArray
{
    val charArray = hexa.toCharArray()
    return ByteArray(charArray.size / 2) { fromHexa(charArray[it * 2], charArray[it * 2 + 1]) }
}

@SuppressLint("WrongConstant")
private fun keyStore(context: Context): KeyStore
{
    val keyStore = KeyStore.getInstance(AndroidKeyStore)
    keyStore.load(null)

    if (!keyStore.containsAlias(KEY_ALIAS))
    {
        val dateStart = GregorianCalendar()
        val dateEnd = GregorianCalendar()
        dateEnd.add(GregorianCalendar.YEAR, 42)
        val keyPairGeneratorSpec =
            KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setKeyType(RSA)
                .setKeySize(KEY_SIZE)
                .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4))
                .setSubject(X500Principal("CN=$KEY_ALIAS"))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(dateStart.time)
                .setEndDate(dateEnd.time)
                .build()
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA, AndroidKeyStore)
        keyPairGenerator.initialize(keyPairGeneratorSpec)
        keyPairGenerator.genKeyPair()
    }

    return keyStore
}

fun deleteKeyStore()
{
    val keyStore = KeyStore.getInstance(AndroidKeyStore)
    keyStore.load(null)

    if (keyStore.containsAlias(KEY_ALIAS))
    {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}

fun encrypt(context: Context, clearStream: InputStream, encryptedStream: OutputStream)
{
    val keyStore = keyStore(context)
    val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    val cipher = Cipher.getInstance(RSA_MODE)
    cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)
    val cipherOutputStream = CipherOutputStream(encryptedStream, cipher)
    val buffer = ByteArray(4096)
    var read = clearStream.read(buffer)

    while (read > 0)
    {
        cipherOutputStream.write(buffer, 0, read)
        read = clearStream.read(buffer)
    }

    cipherOutputStream.flush()
    cipherOutputStream.close()
    clearStream.close()
}

fun encrypt(context: Context, clearText: String): String
{
    val keyStore = keyStore(context)
    val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    val cipher = Cipher.getInstance(RSA_MODE)
    cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)
    val byteArrayOutputStream = ByteArrayOutputStream()
    val cipherOutputStream = CipherOutputStream(byteArrayOutputStream, cipher)
    cipherOutputStream.write(clearText.toByteArray(Charsets.UTF_8))
    cipherOutputStream.close()
    return toHexa(byteArrayOutputStream.toByteArray())
}

fun encryptName(context: Context, name: String): String
{
    val keyStore = keyStore(context)
    val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    val publicKey = privateKeyEntry.certificate.publicKey.encoded
    val size = publicKey.size
    val bytes = name.toByteArray(Charsets.UTF_8)
    val encrypted =
        ByteArray(bytes.size) { (((bytes[it].toInt() and 0xFF) + (publicKey[it % size].toInt() and 0xFF)) and 0xFF).toByte() }
    return "N${toHexa(encrypted)}"
}

fun <P : Parcelable> encryptParcelable(context: Context, parcelable: P): String
{
    val parcel = Parcel.obtain()
    parcelable.writeToParcel(parcel, parcelable.describeContents())
    val clearStream = ByteArrayInputStream(parcel.marshall())
    parcel.recycle()
    val encryptedStream = ByteArrayOutputStream()
    encrypt(context, clearStream, encryptedStream)
    return toHexa(encryptedStream.toByteArray())
}

fun decrypt(context: Context, encryptedStream: InputStream, clearStream: OutputStream)
{
    val keyStore = keyStore(context)
    val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    val cipher = Cipher.getInstance(RSA_MODE)
    cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
    val cipherInputStream = CipherInputStream(encryptedStream, cipher)
    val buffer = ByteArray(4096)
    var read = cipherInputStream.read(buffer)

    while (read > 0)
    {
        clearStream.write(buffer, 0, read)
        read = cipherInputStream.read(buffer)
    }

    clearStream.flush()
    clearStream.close()
    cipherInputStream.close()
}

fun decrypt(context: Context, encryptedText: String): String
{
    val keyStore = keyStore(context)
    val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    val cipher = Cipher.getInstance(RSA_MODE)
    cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
    val byteArrayInputStream = ByteArrayInputStream(fromHexa(encryptedText))
    val cipherInputStream = CipherInputStream(byteArrayInputStream, cipher)
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(4096)
    var read = cipherInputStream.read(buffer)

    while (read > 0)
    {
        byteArrayOutputStream.write(buffer, 0, read)
        read = cipherInputStream.read(buffer)
    }

    byteArrayOutputStream.flush()
    cipherInputStream.close()
    return String(byteArrayOutputStream.toByteArray(), Charsets.UTF_8)
}

fun <P : Parcelable> decryptParcelable(context: Context, clazz: Class<P>, encrypted: String): P
{
    val encryptedStream = ByteArrayInputStream(fromHexa(encrypted))
    val clearStream = ByteArrayOutputStream()
    decrypt(context, encryptedStream, clearStream)
    val data = clearStream.toByteArray()
    val creator = clazz.getDeclaredField("CREATOR").get(null) as Parcelable.Creator<P>
    val parcel = Parcel.obtain()
    parcel.unmarshall(data, 0, data.size)
    parcel.setDataPosition(0)
    val parcelable = creator.createFromParcel(parcel)
    parcel.recycle()
    return parcelable
}