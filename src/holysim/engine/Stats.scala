package holysim.engine

import holysim.utils.Reactive

trait Stats {
	def ReactiveStat = Reactive(0)

	val intellect, crit, haste, mastery, multistrike, versatility = ReactiveStat
}
