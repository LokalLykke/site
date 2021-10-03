package lokallykke.model.session

import java.sql.Timestamp

case class UserSession(
                      sessionId : Long,
                      ip : String,
                      started : Timestamp
                      )
