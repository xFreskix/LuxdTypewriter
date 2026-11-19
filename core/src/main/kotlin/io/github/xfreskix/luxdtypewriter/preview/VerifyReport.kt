package io.github.xfreskix.luxdtypewriter.preview

/** One problem found by [ProjectVerifier]. */
class VerifyProblem(val subject: String, val message: String)

class VerifyReport(val checkedEntries: Int, val problems: List<VerifyProblem>)
