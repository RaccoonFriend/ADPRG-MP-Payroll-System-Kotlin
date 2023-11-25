********************
Last names: Ambrosio, Cipriaso, De Veyra, Iral II
Language: Kotlin
Paradigm(s): Functional, Object Oriented
********************

import java.util.*
import kotlin.collections.ArrayList

val sc = Scanner(System.`in`)
val dl = DailyLog()
var weeklyLog = ArrayList<DailyLog>()
val config = Config()
val dayType = listOf("Normal", "Rest Day", "Regular Holiday", "Regular Holiday and Rest Day", "Special Non-Working Day", "Special Non-Working Day and Rest Day")


// Checks if its been more than 12 hours
fun pastMid(): Boolean {
    if(dl.outTime < dl.inTime) {return true}
    else return false
}

// Checks if overtime
fun isOver(): Boolean {
    var tempOut = dl.outTime.toInt()
    if(tempOut <= 600){
        tempOut+=2400
    }

    if(tempOut > (dl.hrsWorked * 100 + 100 + dl.inTime.toInt())){return true}
    else return false
}

// Between 10pm to 6am is night time
fun isNight(): Boolean{
    var tempOut = dl.outTime.toInt()
    if(tempOut <= 600){
        tempOut+=2400
    }
    if(tempOut in 2200..3000) {return true}
    else return false
}

// Solves how many hours are spent in night shift
fun computeNightHrs(): Int{
    var nightHrs = 0
    if(pastMid()){
        nightHrs = (dl.outTime.toInt()+2400) - 2200
    }
    else nightHrs = dl.outTime.toInt() - 2200
    return nightHrs/100
}
// Solves how many hours are overtime
fun computeOvertimeHrs(): Int{
    var overtimeHrs = 0
    if(pastMid()){
        overtimeHrs = (dl.outTime.toInt()+2400) - (dl.inTime.toInt() + config.maxRegHrs * 100 + 100)

    }
    else overtimeHrs = dl.outTime.toInt() - (dl.inTime.toInt() + config.maxRegHrs * 100 + 100)
    return overtimeHrs / 100
}

fun computeRegOvertimeHrs(): Int{
    var regOvertimeHrs = 0
    var overtimeHrs = 0
    var nightHrs = 0
    if(pastMid()){
        overtimeHrs = (dl.outTime.toInt()+2400) - (dl.inTime.toInt() + config.maxRegHrs * 100 + 100)
        nightHrs = (dl.outTime.toInt()+2400) - 2200

    }
    else {overtimeHrs = dl.outTime.toInt() - (dl.inTime.toInt() + config.maxRegHrs * 100 + 100)
          nightHrs = dl.outTime.toInt() - 2200
    }
    if(nightHrs > 0){
        regOvertimeHrs = (overtimeHrs - nightHrs)/100
    }
    else regOvertimeHrs = overtimeHrs/100

    // emergency testing prints
    // println("regOvertimeHrs: $regOvertimeHrs")
    return regOvertimeHrs
}


fun computeOvertime(record: DailyLog): Float {
    var hourlyRate = config.defRate/config.maxRegHrs
    val overtimeRates = mapOf(
        Pair("Normal", 1.25f), //125% for overtime on normal days
        Pair("Rest Day", 1.69f), //169% for overtime on rest days
        Pair("Special Non-Working Day", 1.69f), //169% for overtime on special non-working days
        Pair("Special Non-Working Day and Rest Day", 1.95f), //195% for overtime on this
        Pair("Regular Holiday", 2.6f) //260% for overtime on regular holidays
    )
    val nightShiftRates = mapOf(
        Pair("Normal", 1.375f), //137.5% for overtime on normal days
        Pair("Rest Day", 1.859f), //185.9% for overtime on rest days
        Pair("Special Non-Working Day", 1.859f), //185.9% for overtime on special non-working days
        Pair("Special Non-Working Day and Rest Day", 2.145f), //214.5% for overtime on this again
        Pair("Regular Holiday", 2.86f) //286% for overtime on regular holidays
    )
    var overtimePay = 0.0f

    if(isOver()){
        if (record.dayType in overtimeRates) {
            overtimePay += ((computeRegOvertimeHrs() * hourlyRate) * overtimeRates[dl.dayType]!!)
        }
    }
    if(isNight()){
        if(record.dayType in nightShiftRates){
            overtimePay += ((computeNightHrs() * hourlyRate) * nightShiftRates[record.dayType]!!)
        }
    }
    return overtimePay
}

private fun computePremiumPay(record: DailyLog, hourlyRate: Float): Float {
    val payRates = mapOf(
        Pair("Normal", 1.0f),
        Pair("Rest Day", 1.3f), //130% for rest days
        Pair("Special Non-Working Day", 1.3f), //130% for special non-working days
        Pair("Special Non-Working Day and Rest Day", 1.5f), //150% for whatever this is
        Pair("Regular Holiday", 2.0f), //200% for regular holidays
        Pair("Regular Holiday and Rest Day", 2.6f) //260% for this combination
    )

    var pay = 0.0f
    if (record.dayType in payRates) {
        val dayRate = hourlyRate * record.hrsWorked * payRates[record.dayType]!!
        pay += dayRate

    }
    return pay
}

fun computeDailySalary(): DailyLog {
    print("Enter OUT time (HHmm): ")
    var outTime = sc.next()
    if (outTime.toInt() < 0 || outTime.toInt() > 2400) {
        println("Invalid input. Using ${config.defOut} as the default OUT time.")
        dl.outTime = config.defOut
    }

    else{dl.outTime = outTime}

    println("Enter day type")
    for ((index, day) in dayType.withIndex()) {
        println("[${index+1}] $day")
    }
    val input = sc.nextInt() - 1
    val selectedDayType = if (input in 0 until dayType.size) {
        dayType[input]
    } else {
        println("Invalid input. Using ${config.defDay} as the default day type.")
        config.defDay
    }
    dl.dayType = selectedDayType
    var hourlyRate = config.defRate/config.maxRegHrs
    var salary = 0.0f

    /* emergency testing prints
    println("isNight: ${isNight()}")
    println("isOver: ${isOver()}")
    println("computeOvertimeHrs: ${computeOvertimeHrs()}   computeNightHrs: ${computeNightHrs()}")*/

    if (!dl.absent){
        if (selectedDayType != "Normal" || isNight()) {
            salary = computePremiumPay(dl, hourlyRate)
        } else {
            salary = config.defRate
        }

        if(isOver() || isNight()){
            salary += computeOvertime(dl)
        }

        /*emergency testing prints
        println("hourlyRate: $hourlyRate")
        println("computerPremiumPay: ${computePremiumPay(dl, hourlyRate)}")
        println("computerOvertime: ${computeOvertime(dl)}")*/

    }
    val newLog = DailyLog(
        config = config,
        dailyRate = config.defRate,
        inTime = config.defIn,
        outTime = config.defOut,
        dayType = config.defDay,
        overtime = 0,
        dailySalary = salary,
        hrsWorked = config.maxRegHrs,
        absent = false
    )
    println("Daily Salary: ${newLog.dailySalary}\n")
    return newLog
}

fun computeWeeklySalary(): Float {
    var weekTotalSalary = 0.0f;
    for (i in 0 until 7) {
        weekTotalSalary += weeklyLog.get(i).dailySalary

        if(i > config.maxRegDays){
            weeklyLog.get(i).dayType = "Rest Day"
        }
    }
    return  weekTotalSalary
}

fun createPayroll() {
    println("\nInput out time in Military Format (Sunday through Saturday):\n")
    for (i in 0 until 7) {
        weeklyLog.add(i, computeDailySalary())
        println("******************************************************\n")
    }
    val weekTotalSalary = computeWeeklySalary()
    println("Total Salary for The Week: $weekTotalSalary")
}

fun configureDefaults() {
    while(true) {
        println("----------------------------------------------------")
        println("[DEFAULT CONFIGURATIONS]")
        println("[1] Daily Rate (Current: ${config.defRate})")
        println("[2] Max Daily Work Hours (Current: ${config.maxRegHrs})")
        println("[3] Max Weekly Work Days (Current: ${config.maxRegDays})")
        println("[4] Default In-Time (Current: ${config.defIn})")
        println("[5] Default Out-Time (Current: ${config.defOut})")
        println("[6] Default Day Type (Current: ${config.defDay})")
        println("[7] Back")
        println("Select Menu: ")
        val choice = sc.nextInt();
        when (choice) {
            1 -> { println("Enter New Daily Rate: ")
                config.defRate  = sc.nextFloat()
                println("Successfully Updated!\n")
            }
            2 -> { println("Enter New Max Daily Work Hours: ")
                config.maxRegHrs = sc.nextInt()
                println("Successfully Updated!\n")
            }
            3 -> { println("Enter New Max Weekly Work Days : ")
                config.maxRegDays = sc.nextInt()
                println("Successfully Updated!\n")
            }
            4 -> { println("Enter New Default In-Time: ")
                config.defIn = sc.next()
                println("Successfully Updated!\n")
            }
            5 -> { println("Enter New Default In-Time: ")
                config.defOut = sc.next()
                println("Successfully Updated!\n")
            }
            6 -> { println("(Valid Day types): Normal, Rest Day, Regular Holiday, Regular Holiday and Rest Day, Special Non-Working Day, Special Non-Working Day and Rest Day\n Enter New Default Day type: ")
                config.defIn = sc.next()
                println("Successfully Updated!\n")
            }
            7 -> break
        }
    }
}

fun mainMenu(){
    while(true) {
        println("----------------------------------------------------")
        println("Weekly Payroll System")
        println("[1] Simulate A day's Salary")
        println("[2] Create Payroll")
        println("[3] Configure Default Settings")
        println("[4] Exit Program")
        print("Select Menu: ")
        val choice = sc.nextInt();
        when (choice) {
            1 -> {
                println("\n-- Calculate Daily Salary --")
                computeDailySalary()
            }
            2 -> createPayroll()
            3 -> configureDefaults()
            4 -> break
        }
    }
    return
}

fun main(args: Array<String>) {
    mainMenu()
}
