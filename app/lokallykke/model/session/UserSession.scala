package lokallykke.model.session

import java.sql.Timestamp

case class UserSession(
                      sessionId : Long,
                      email : Option[String],
                      authenticated : Int,
                      nonce : Option[String],
                      state : Option[String],
                      expires : Option[Timestamp]
                      )
