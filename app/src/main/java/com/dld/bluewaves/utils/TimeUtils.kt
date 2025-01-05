import android.content.Context
import android.text.format.DateUtils
import com.dld.bluewaves.R
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun getRelativeTime(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()

        val difference = now - timestamp
        return when {
            difference < DateUtils.MINUTE_IN_MILLIS -> context.getString(R.string.just_now)
            difference < DateUtils.HOUR_IN_MILLIS -> {
                val minutes = (difference / DateUtils.MINUTE_IN_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.minutes_ago, minutes, minutes)
            }
            difference < DateUtils.DAY_IN_MILLIS -> {
                val hours = (difference / DateUtils.HOUR_IN_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.hours_ago, hours, hours)
            }
            difference < DateUtils.WEEK_IN_MILLIS -> {
                val days = (difference / DateUtils.DAY_IN_MILLIS).toInt()
                if (days == 1) context.getString(R.string.yesterday)
                else context.resources.getQuantityString(R.plurals.days_ago, days, days)
            }
            else -> {
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                format.format(Date(timestamp))
            }
        }
    }

    fun formatDateToFullMonthDayYearWithTime(timestamp: Long): String {
        val format = SimpleDateFormat("MMMM dd, yyyy | h:mma", Locale.getDefault())
        return format.format(Date(timestamp)).lowercase(Locale.getDefault())
    }
}