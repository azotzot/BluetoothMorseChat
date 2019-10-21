package azotzot.bluetoothmorsechat

class MorseDecoder(lang: Int) {
    companion object {
        const val ru = 0
        const val en = 1
    }

    private val language: Int = lang

    private val russianMorseAlphabet: HashMap<String,String> = hashMapOf(
        Pair("*-",    "а"),
        Pair("-***",  "б"),
        Pair("*--",   "в"),
        Pair("--*",   "г"),
        Pair("-**",   "д"),
        Pair("*",     "е"),
        Pair("***-",  "ж"),
        Pair("--**",  "з"),
        Pair("**",    "и"),
        Pair("*---",  "й"),
        Pair("-*-",   "к"),
        Pair("*-**",  "л"),
        Pair("--",    "м"),
        Pair("-*",    "н"),
        Pair("---",   "о"),
        Pair("*--*",  "п"),
        Pair("*-*",   "р"),
        Pair("***",   "с"),
        Pair("-",     "т"),
        Pair("**-",   "у"),
        Pair("**-*",  "ф"),
        Pair("****",  "х"),
        Pair("-*-*",  "ц"),
        Pair("---*",  "ч"),
        Pair("----",  "ш"),
        Pair("--*-",  "щ"),
        Pair("*--*-*","ъ"),
        Pair("-*--",  "ы"),
        Pair("-**-",  "ь"),
        Pair("**-**", "э"),
        Pair("**--",  "ю"),
        Pair("*-*-",  "я"),
        Pair("-----", "0"),
        Pair("*----", "1"),
        Pair("**---", "2"),
        Pair("***--", "3"),
        Pair("****-", "4"),
        Pair("*****", "5"),
        Pair("-****", "6"),
        Pair("--***", "7"),
        Pair("---**", "8"),
        Pair("----*", "9")
    )

    private val englishMorseAlphabet: HashMap<String,String> = hashMapOf(
        Pair("*-",    "a"),
        Pair("-***",  "b"),
        Pair("-*-*",  "c"),
        Pair("-**",   "d"),
        Pair("*",     "e"),
        Pair("**-*",  "f"),
        Pair("--*",   "g"),
        Pair("****",  "h"),
        Pair("**",    "i"),
        Pair("*---",  "j"),
        Pair("-*-",   "k"),
        Pair("*-**",  "l"),
        Pair("--",    "m"),
        Pair("-*",    "n"),
        Pair("---",   "o"),
        Pair("*--*",  "p"),
        Pair("--*-",  "q"),
        Pair("*-*",   "r"),
        Pair("***",   "s"),
        Pair("-",     "t"),
        Pair("**-",   "u"),
        Pair("***-",  "v"),
        Pair("*--",   "w"),
        Pair("-**-",  "x"),
        Pair("-*--",  "y"),
        Pair("--**",  "z"),
        Pair("-----", "0"),
        Pair("*----", "1"),
        Pair("**---", "2"),
        Pair("***--", "3"),
        Pair("****-", "4"),
        Pair("*****", "5"),
        Pair("-****", "6"),
        Pair("--***", "7"),
        Pair("---**", "8"),
        Pair("----*", "9")
    )

    fun decodeChar(code: String): String = when(language) {
            ru -> if (russianMorseAlphabet.containsKey(code)) russianMorseAlphabet[code].toString() else ""
            en -> if (englishMorseAlphabet.containsKey(code)) englishMorseAlphabet[code].toString() else ""
        else -> "&"
    }





}