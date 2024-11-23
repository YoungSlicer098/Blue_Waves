import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

fun getAccessToken(context: Context): String {

    val keyStream: InputStream = context.assets.open("bluewaves-dld-fefb0182362f.json")

    val credentials = GoogleCredentials.fromStream(keyStream)
        .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
    credentials.refreshIfExpired()
    return credentials.accessToken.tokenValue
}