package danilovl.calculator.domain

import danilovl.calculator.data.model.CurrencyInfo

class CurrencySearchService {

    fun search(
        all: List<CurrencyInfo>,
        exclude: Collection<String>,
        query: String
    ): List<CurrencyInfo> {
        val q = query.trim()
        val candidates = all.filter { it.code !in exclude }
        if (q.isEmpty()) return candidates

        return candidates
            .filter { matches(it, q) }
            .sortedWith(rankComparator(q))
    }

    private fun matches(currency: CurrencyInfo, q: String): Boolean {
        return currency.code.contains(q, ignoreCase = true) ||
               currency.name.contains(q, ignoreCase = true)
    }

    private fun rankComparator(q: String): Comparator<CurrencyInfo> {
        return compareByDescending<CurrencyInfo> { it.code.equals(q, ignoreCase = true) }
            .thenByDescending { it.code.startsWith(q, ignoreCase = true) }
            .thenByDescending { it.code.contains(q, ignoreCase = true) }
            .thenByDescending { it.name.startsWith(q, ignoreCase = true) }
    }
}
