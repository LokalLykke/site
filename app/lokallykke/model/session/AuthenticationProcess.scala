package lokallykke.model.session

import java.sql.Timestamp

case class AuthenticationProcess(
                                sessionId : Long,
                                nonce : String,
                                state : String,
                                forwardUrl : String,
                                email : Option[String],
                                validUntil : Option[Timestamp]
                                )
