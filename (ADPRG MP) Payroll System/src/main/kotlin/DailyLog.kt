data class DailyLog (
    val config: Config = Config(),
    var dailyRate: Float = config.defRate,
    var inTime: String = config.defIn,
    var outTime: String = config.defOut,
    var dayType: String = config.defDay,
    var overtime: Int = 0,
    var dailySalary: Float = 0.0f,
    var hrsWorked: Int = config.maxRegHrs,
    var absent: Boolean = false
    )


