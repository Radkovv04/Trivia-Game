package com.example.bulgariatriviaconquest

// The structure of a question
data class TriviaQuestion(
    val category: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

object QuestionBank {
    // This list keeps track of questions we haven't asked yet this game
    private var availableQuestions = mutableListOf<TriviaQuestion>()

    // Run this when starting a NEW GAME
    fun resetQuestions() {
        availableQuestions.clear()
        availableQuestions.addAll(allQuestions.shuffled()) // Shuffles them randomly!
    }

    // Pulls a random question and removes it from the pool so it doesn't repeat
    fun getNextQuestion(): TriviaQuestion {
        if (availableQuestions.isEmpty()) {
            resetQuestions() // Failsafe: If we run out of all 100 questions, reshuffle and start over
        }
        return availableQuestions.removeAt(0)
    }

    // Your Master Database of Questions
    private val allQuestions = listOf(

        // --- ИСТОРИЯ (HISTORY) ---
        TriviaQuestion("История", "През коя година е създадена Първата българска държава?", listOf("681 г.", "864 г.", "1014 г.", "1185 г."), 0),
        TriviaQuestion("История", "Кой български владетел покръства българите?", listOf("Хан Аспарух", "Княз Борис I", "Цар Симеон Велики", "Цар Иван Асен II"), 1),
        TriviaQuestion("История", "Къде е подписан Санстефанският мирен договор?", listOf("В Търново", "В София", "В Сан Стефано", "В Берлин"), 2),
        TriviaQuestion("История", "През коя година избухва Априлското въстание?", listOf("1876 г.", "1878 г.", "1885 г.", "1908 г."), 0),
        TriviaQuestion("История", "Кой е авторът на 'История славянобългарска'?", listOf("Христо Ботев", "Иван Вазов", "Паисий Хилендарски", "Софроний Врачански"), 2),

        // --- ГЕОГРАФИЯ (GEOGRAPHY) ---
        TriviaQuestion("География", "Кой е най-високият връх в България?", listOf("Вихрен", "Мусала", "Ботев", "Черни връх"), 1),
        TriviaQuestion("География", "Коя е най-дългата река, изцяло протичаща на територията на България?", listOf("Марица", "Искър", "Дунав", "Струма"), 1),
        TriviaQuestion("География", "Кой град е известен като 'Морската столица' на България?", listOf("Бургас", "Несебър", "Варна", "Созопол"), 2),
        TriviaQuestion("География", "В коя планина се намират Седемте рилски езера?", listOf("Пирин", "Рила", "Родопи", "Стара планина"), 1),
        TriviaQuestion("География", "С коя държава България граничи на север?", listOf("Гърция", "Сърбия", "Турция", "Румъния"), 3),

        // --- ЛИТЕРАТУРА (LITERATURE) ---
        TriviaQuestion("Литература", "Кой е авторът на романа 'Под игото'?", listOf("Елин Пелин", "Йордан Йовков", "Иван Вазов", "Алеко Константинов"), 2),
        TriviaQuestion("Литература", "Как се казва героят на Алеко Константинов?", listOf("Бай Ганьо", "Хитър Петър", "Андрешко", "Бойчо Огнянов"), 0),
        TriviaQuestion("Литература", "Кое стихотворение започва с 'Тежко, тежко! Вино дайте!'?", listOf("Хаджи Димитър", "В механата", "Обесването на Васил Левски", "Опълченците на Шипка"), 1),
        TriviaQuestion("Литература", "Кой български поет е известен като 'Поетът-революционер'?", listOf("Пейо Яворов", "Димчо Дебелянов", "Христо Ботев", "Никола Вапцаров"), 2),
        TriviaQuestion("Литература", "Кой е авторът на повестта 'Гераците'?", listOf("Елин Пелин", "Йордан Йовков", "Димитър Талев", "Иван Вазов"), 0),

        // --- БЪЛГАРСКИ ЕЗИК И ГРАМАТИКА (GRAMMAR) ---
        TriviaQuestion("Граматика", "Колко букви има в съвременната българска азбука?", listOf("28", "29", "30", "31"), 2),
        TriviaQuestion("Граматика", "Коя от следните думи е синоним на 'красив'?", listOf("Грозен", "Хубав", "Умен", "Бърз"), 1),
        TriviaQuestion("Граматика", "Каква част на речта е думата 'бързо'?", listOf("Съществително име", "Прилагателно име", "Наречие", "Глагол"), 2),
        TriviaQuestion("Граматика", "Къде е правилно да се постави пълен член?", listOf("Човекът дойде.", "Видях човекът.", "Дадох на човекът.", "Говорих с човекът."), 0),
        TriviaQuestion("Граматика", "Коя дума е написана ГРЕШНО?", listOf("Отвертка", "Сграда", "Въстание", "Очастник"), 3),

        // --- МАТЕМАТИКА В БЪЛГАРИЯ (MATH - BULGARIA THEMED) ---
        TriviaQuestion("Математика", "Разстоянието от София до Бургас е около 380 км. Ако карате със 100 км/ч, за колко време ще стигнете?", listOf("2 часа и 48 мин", "3 часа и 48 мин", "4 часа", "4 часа и 20 мин"), 1),
        TriviaQuestion("Математика", "Ако един билет за Рилския манастир струва 8 лв, колко ще платят група от 12 ученици?", listOf("86 лв.", "92 лв.", "96 лв.", "108 лв."), 2),
        TriviaQuestion("Математика", "Връх Ботев е висок 2376 м, а връх Мусала - 2925 м. С колко метра Мусала е по-висок?", listOf("539 м", "549 м", "559 м", "569 м"), 1),
        TriviaQuestion("Математика", "България е основана през 681 г. На колко години е станала държавата през 2024 г.?", listOf("1341", "1342", "1343", "1344"), 2),
        TriviaQuestion("Математика", "Ако в една българска народна носия има 45 червени шевици и 30 черни, какво е съотношението на червени към черни?", listOf("2:1", "3:2", "4:3", "5:4"), 1)

        // You can keep pasting more questions here using the exact same format!
    )
}