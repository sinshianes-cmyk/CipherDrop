package android.net
class Uri(private val s: String) { override fun toString() = s; companion object { fun parse(s: String) = Uri(s) } }
