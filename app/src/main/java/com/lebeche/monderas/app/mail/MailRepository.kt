package com.lebeche.monderas.app.mail

import java.util.Properties
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.InternetAddress
import com.sun.mail.imap.IMAPFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MailSummary(
    val uid: Long,
    val from: String,
    val subject: String,
    val date: String,
    val seen: Boolean,
)

data class MailDetail(
    val from: String,
    val subject: String,
    val date: String,
    val body: String,
)

/** Acceso IMAP al buzón de las Monderas (JavaMail). */
object MailRepository {

    private fun props(): Properties = Properties().apply {
        put("mail.store.protocol", "imaps")
        put("mail.imaps.host", MailConfig.IMAP_HOST)
        put("mail.imaps.port", MailConfig.IMAP_PORT.toString())
        put("mail.imaps.ssl.enable", "true")
        put("mail.imaps.connectiontimeout", "20000")
        put("mail.imaps.timeout", "20000")
    }

    fun connectStore(email: String, password: String): Store {
        val session = Session.getInstance(props())
        val store = session.getStore("imaps")
        store.connect(MailConfig.IMAP_HOST, MailConfig.IMAP_PORT, email, password)
        return store
    }

    suspend fun fetchInbox(email: String, password: String, limit: Int = 50): List<MailSummary> =
        withContext(Dispatchers.IO) {
            val store = connectStore(email, password)
            try {
                val inbox = store.getFolder("INBOX") as IMAPFolder
                inbox.open(Folder.READ_ONLY)
                inbox.messages.reversed().take(limit).mapNotNull { m ->
                    runCatching {
                        MailSummary(
                            uid = inbox.getUID(m),
                            from = from(m),
                            subject = m.subject.orEmpty(),
                            date = m.receivedDate?.toString().orEmpty(),
                            seen = m.isSet(javax.mail.Flags.Flag.SEEN),
                        )
                    }.getOrNull()
                }
            } finally {
                runCatching { store.close() }
            }
        }

    suspend fun fetchDetail(email: String, password: String, uid: Long): MailDetail? =
        withContext(Dispatchers.IO) {
            val store = connectStore(email, password)
            try {
                val inbox = store.getFolder("INBOX") as IMAPFolder
                inbox.open(Folder.READ_ONLY)
                val m = inbox.getMessageByUID(uid) ?: return@withContext null
                MailDetail(
                    from = from(m),
                    subject = m.subject.orEmpty(),
                    date = m.receivedDate?.toString().orEmpty(),
                    body = extractBody(m),
                )
            } finally {
                runCatching { store.close() }
            }
        }

    private fun from(m: Message): String =
        (m.from?.firstOrNull() as? InternetAddress)?.address
            ?: m.from?.firstOrNull()?.toString().orEmpty()

    private fun extractBody(m: Message): String = runCatching {
        when {
            m.isMimeType("text/plain") -> m.content?.toString().orEmpty()
            m.isMimeType("text/html") -> m.content?.toString().orEmpty()
            m.isMimeType("multipart/*") -> {
                val mp = m.content as javax.mail.Multipart
                (0 until mp.count).mapNotNull { bp ->
                    val p = mp.getBodyPart(bp)
                    if (p.isMimeType("text/plain") || p.isMimeType("text/html")) {
                        p.content?.toString()
                    } else null
                }.joinToString("\n")
            }
            else -> m.content?.toString().orEmpty()
        }
    }.getOrDefault("")
}
