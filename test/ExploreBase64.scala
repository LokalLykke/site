import lokallykke.security.Encryption
import play.api.libs.json.Json

import java.util.Base64

object ExploreBase64 {

  def main(args: Array[String]): Unit = {
    val str = """{
                |  "access_token": "ya29.a0ARrdaM-akPsEh8rXX2D8f5dg6qazq7E2zm-CwEqu84lXxQAZ2fuN70081skP4M7AiPNbj4U-LbDQt_W5aLnpttyJoXeNLojmj-wghbdfUT2CqhwWc9clovG6Q5Z3jjT1ncz3I6SH1sRqbFYsnsqM0nHkJTQ_",
                |  "expires_in": 3599,
                |  "scope": "https://www.googleapis.com/auth/userinfo.email openid",
                |  "token_type": "Bearer",
                |  "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjhkOTI5YzYzZmYxMDgyYmJiOGM5OWY5OTRmYTNmZjRhZGFkYTJkMTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MzMxOTQzMDMzMjEtZWtrYmtxM29tODJsNGh0czNxam9vMjNxZzBpMGVkbm0uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MzMxOTQzMDMzMjEtZWtrYmtxM29tODJsNGh0czNxam9vMjNxZzBpMGVkbm0uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDAxMzA0NjgyNDI1ODI1MzIyODMiLCJlbWFpbCI6Imxva2FsbHlra2VAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJBQldsbk1sQTZ5RENUUFR2aUFYbGtnIiwibm9uY2UiOiJxeTZpNmU4SWZ5R0hnRDI2MXpMZExRRy9WN2RBcXlvSE1vWExsczdSOFZEa2Rnb3lUcEdaUDhob3pKWlZuQSsrM0RaYnBUY3J0ZTdKZnp3ckp6RWV5dkNwYTVCMGYzOVJiQ1NPbFJWeUU1aTRxd1FySEw3bFdSUHdPWDdzeDIzYyIsImlhdCI6MTYzMzU0MzI3MSwiZXhwIjoxNjMzNTQ2ODcxfQ.0rxd7UimP933_2dlU_2EmFy8mud2pCqE5aTpjBSRE4nvAqEUP0GZ6CrwxJE3xcEoInkWt7i-uuxz7dJ_r6d3-z1PQLxTSmvijAHleMCAGq77PzLLG99iNxRe_3Uxe0iaJYGNJlMeJ3VqkrLsQTKDDmBQwCQpLffU95t2nA1AQYixp8YhaWOCHlwPq9zVV1MFjoV6V_k-HwphOPJSV5eDBtJhFEZXaj9tcW9UponqpaXdktWtoiFntsLytINTmdn2vABOdOQSLFo-FQlO4eflsNBmqNXjfWvcMKVWjVDtWYzWWiSo6VWQVOd9hEK35PTD5no6fCZinT9Qs8lgPu1nhg"
                |}""".stripMargin
    val js = Json.parse(str)
    println(js)
  }

}
