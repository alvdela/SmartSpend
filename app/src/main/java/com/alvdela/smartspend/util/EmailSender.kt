package com.alvdela.smartspend.util

import com.typesafe.config.ConfigFactory
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.concurrent.ExecutorService

class EmailSender {
    private val config = ConfigFactory.load()
    private val username: String = config.getString("email.username")
    private val password: String = config.getString("email.password")

    private val props: Properties = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }

    private val session: Session = Session.getInstance(props,
        object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

    @Throws(MessagingException::class)
    fun sendEmail(to: String, subject: String, body: String) {
        val message: Message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            setText(body)
        }
        Transport.send(message)
    }

   /* @Throws(MessagingException::class)
    fun sendEmailAsync(to: String, subject: String, body: String) {
        executor.submit {
            sendEmail(to, subject, body)
        }
    }*/
}
