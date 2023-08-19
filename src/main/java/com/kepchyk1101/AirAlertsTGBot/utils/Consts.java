package com.kepchyk1101.AirAlertsTGBot.utils;

public class Consts {

    // Текста, у меня пока не получилось адекватно вынести их в кфг. файл, поэтому пока-что так, в запечатанном виде.
    public static final String START_MESSAGE =
            "Привіт! \uD83D\uDC4B Я - бот, який може тебе повідомляти про повітряну(-і) тривогу(-и) в твоєму, або одразу в декількох містах та областях!"; // 👋

    public static final String HELP_MESSAGE =
            """
            <b>Трохи загальної інформації</b>
            Це неофіційний бот, зроблений заради інтересу та досвіду у цій сфері.
            Робота бота безпосередньо залежить від працездатності сервісу alerts.com.ua, так що якщо у них будуть якісь проблеми з API - то і у бота швидше за все будуть проблеми з відображенням інформації.
            Автооновлення даних про тривоги – кожні 10 секунд.
            Крим відсутній у списку, оскільки по ньому відсутня інформація. Але ми всі знаємо, що Крим - це Україна.
            Також ви можете додати бота до групового чату і там він теж буде працювати.
            
            <b>Команди:</b>
            /start - перезапустити бота
            /help - потрапити до цього меню
            /add - додати підписку(-и)
            /remove - видалити підписку(-и)
            /subs - переглянути ваші підписки
            /status - дізнатися стан тривог
            
            <b>Зворотний зв'язок</b>
            Якщо у вас залишилися питання - напишіть розробнику: @Kepchyk1101
            """;

    public static final String ALREADY_REGISTERED_MESSAGE =
            "ℹ · Ви вже зареєстровані і маєте отримувати повідомлення.";

    public static final String ADD_REGIONS_MESSAGE =
            """
            ℹ · Будь ласка, оберіть вашу область та місто, щоб почати отримувати повідомлення:
            (Можна обрати декілька)
            """;

    public static final String REGION_REMOVING_MESSAGE =
            "ℹ · Будь ласка, оберіть ті міста та області, які ви хочете видалити (Щоб не отримувати повідомлень. Можна обрати декілька).";

    public static final String REGION_LIST_MESSAGE =
            """
            ℹ · Ваш список повідомлень:
            
            <b>{regionName}</b>
            Якщо ви бажаєте <b>додати</b> конкретну або усі області та міста, скористайтеся командою: /add
            Якщо ви бажаєте <b>видалити</b> конкретну або усі області та міста, скористайтеся командою: /remove
            """;

    public static final String REGIONS_ALERTSTATUS_MESSAGE =
            """
            ℹ · Актуальні статуси тривог:
            🔴 = Присутня повітряна тривога
            🟢 = Немає повітряної тривоги
            
            {region}
            
            (⚠️ · Інформація береться на основі останньої інформації. Майте це на увазі, якщо сервіс не працює корректно)
            """;

    public static final String START_ALERT =
            "\uD83D\uDD34 <b>· {time} · Повітряна тривога в {regionName}.</b>"; // 🔴

    public static final String CANCEL_ALERT =
            "\uD83D\uDFE2 <b>· {time} · Відбій тривоги в {regionName}.</b>"; // 🟢

    public static final String REGION_SUCCESSFULLY_ADDED_MESSAGE =
            "✅ · Ви додали <b>{regionName}</b> до свого списку повідомлень.";

    public static final String REGION_SUCCESSFULLY_REMOVED_MESSAGE =
            "✅ · Ви видалили <b>{regionName}</b> зі свого списку повідомлень.";

    public static final String REGION_LIST_ERROR_MESSAGE =
            "❌ · У вашому списку повідомлень немає жодної області. Якщо ви бажаєте отримувати повідомлення, скористайтеся командою: /add";

    public static final String REGION_ADDING_ERROR_MESSAGE =
            "❌ · Ви не можете додати нову область або місто, оскільки ви вже отримуєте повідомлення з усіх областей та міст. Якщо ви бажаєте не отримувати повідомлення з усіх міст, скористайтеся командою: /remove";

    public static final String REGION_ADD_ERROR_MESSAGE =
            "❌ · Ви не можете додати <b>{regionName}</b> до списку своїх повідомлень, оскільки ця область/місто вже є у вашому списку.";

    public static final String REGION_REMOVING_ERROR_MESSAGE =
            "❌ · Ви не можете видалити <b>{regionName}</b>, оскільки ви не маєте жодної підписки. Якщо ви бажаєте почати отримувати повідомлення, скористайтеся командою: /add";



    // Эти константы лучше не трогать, оно вам не нужно :)
    public static final String ALL_REGIONS_ID = "Усі області та міста одразу";

    public static final String UNIQUE_ADD_ID = "[add]-";
    public static final String UNIQUE_REMOVE_ID = "[rem]-";

    public static final String ALL_ID = "all";
    public static final String NOTHING_ID = "nothing";

}