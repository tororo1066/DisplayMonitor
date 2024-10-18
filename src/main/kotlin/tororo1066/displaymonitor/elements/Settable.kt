package tororo1066.displaymonitor.elements

@Target(AnnotationTarget.FIELD)
annotation class Settable(val name: String = "", val childOnly: Boolean = false)