package it.uspread.ios

import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService
import grails.transaction.Transactional
import grails.util.Environment
import it.uspread.core.User
import org.codehaus.groovy.grails.io.support.ClassPathResource

@Transactional
class APNSMessageService {

    private ApnsService service

    APNSMessageService() {

        def certPath = null
        def certPass = null
        switch (Environment.current) {
            case Environment.DEVELOPMENT:
                certPath = new ClassPathResource("apns-dev.p12").getInputStream()
                certPass = "purzqqg.2.7"
                break
            // TODO pour l'instant on pointe aussi sur l'environnement de dev
            case Environment.PRODUCTION:
                certPath = new ClassPathResource("apns-dev.p12").getInputStream()
                certPass = "purzqqg.2.7"
                break
        }

        service = APNS.newService()
                .withCert(certPath, certPass)
                .withSandboxDestination()
                .build();
    }

    def push(String token, String message) {
        String payload = APNS.newPayload().alertBody(message).build();
        service.push(token, payload);
    }

    def notifySentTo(List<User> recipients) {
        for (User user : recipients) {
            if (null != user.iosPushToken && user.iosPushToken.length() > 0) {
                push(user.iosPushToken, "New message(s) spreading")
            }
        }
    }
}
