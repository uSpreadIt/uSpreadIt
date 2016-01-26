package it.uspread.core.type

/**
 * Langage supporté (Pour le fonctionnement interne de l'application).<br>
 * Limité en BD à 2 caractères (Code ISO-639-1)
 */
enum Language {
    AF('af', 'Afrikaans', 'Afrikaans'),
    AR('ar', 'Arabic', 'العربية'),
    BG('bg', 'Bulgarian', 'български език'),
    CS('cs', 'Czech', 'Česky'),
    DA('da', 'Danish', 'Dansk'),
    DE('de', 'German', 'Deutsch'),
    EL('el', 'Greek', 'Ελληνικά'),
    EN('en', 'English', 'English'),
    ES('es', 'Spanish', 'Español'),
    ET('et', 'Estonian', 'Eesti keel'),
    FA('fa', 'Persian', 'فارسی'),
    FI('fi', 'Finnish', 'Suomen kieli'),
    FR('fr', 'French', 'Français'),
    HE('he', 'Hebrew', 'עברית'),
    HI('hi', 'Hindi', 'हिन्दी ; हिंदी'),
    HR('hr', 'Croatian', 'Hrvatski'),
    HU('hu', 'Hungarian', 'magyar'),
    ID('id', 'Indonesian', 'Bahasa Indonesia'),
    IT('it', 'Italian', 'Italiano'),
    JA('ja', 'Japanese', '日本語 (にほんご)'),
    KO('ko', 'Korean', '한국어 (韓國語)'),
    LT('lt', 'Lithuanian', 'Lietuvių kalba'),
    LV('lv', 'Latvian', 'Latviešu valoda'),
    MK('mk', 'Macedonian', 'македонски јазик'),
    MR('mr', 'Marathi', 'मराठी'),
    NE('ne', 'Nepali', 'नेपाली'),
    NL('nl', 'Dutch', 'Nederlands'),
    NO('no', 'Norwegian', 'Norsk'),
    PL('pl', 'Polish', 'Polski'),
    PT('pt', 'Portuguese', 'Português'),
    RO('ro', 'Romanian', 'Română'),
    RU('ru', 'Russian', 'русский язык'),
    SK('sk', 'Slovak', 'Slovenčina'),
    SL('sl', 'Slovene', 'Slovenščina'),
    SO('so', 'Somali', 'Soomaaliga'),
    SQ('sq', 'Albanian', 'Shqip'),
    SV('sv', 'Swedish', 'Svenska'),
    SW('sw', 'Swahili', 'Kiswahili'),
    TA('ta', 'Tamil', 'தமிழ்'),
    TH('th', 'Thai', 'ไทย'),
    TL('tl', 'Tagalog', 'Tagalog'),
    TR('tr', 'Turkish', 'Türkçe'),
    UK('uk', 'Ukrainian', 'українська мова'),
    UR('ur', 'Urdu', 'اردو'),
    VI('vi', 'Vietnamese', 'Tiếng Việt'),
    ZH('zh', 'Chinese', '中文, 汉语, 漢語')

    String code
    String name
    String localizedName

    Language(code, name, localizedName) {
        this.code = code
        this.name = name
        this.localizedName = localizedName
    }
}
