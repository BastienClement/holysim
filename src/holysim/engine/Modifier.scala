package holysim.engine

import holysim.utils.Reactive

import scala.math.Numeric

/**
 * ModifierValue constructor
 */
abstract class Modifier[T](val base: T, val fold: (T, T) => T) {
	def apply(value: Reactive[T]) = ModifierValue(this, value)
}

/**
 * Modifer classes for common pattern
 */
object Modifier {
	abstract class Additive extends Modifier[Int](0, (a, b) => a + b)
	abstract class Multiplicative extends Modifier[Double](1.0, (a, b) => a * b)
	abstract class Maximum[T](base: T)(implicit n: Numeric[T]) extends Modifier[T](base, (a, b) => n.max(a, b))
	abstract class Minimum[T](base: T)(implicit n: Numeric[T]) extends Modifier[T](base, (a, b) => n.min(a, b))
	abstract class Unique[T](base: T) extends Modifier[T](base, (a, b) => b)
}

/**
 * The combined modifier with an associated value
 */
case class ModifierValue[T](mod: Modifier[T], value: Reactive[T])

/**
 * Implemented modifiers
 */
object Mod {
	// Import modifiers classes

	import Modifier._

	// Base stats
	object BaseIntellect extends Additive
	object BaseStamina extends Additive
	object BaseSpirit extends Additive

	object IntellectScore extends Additive
	object StaminaScore extends Additive
	object SpiritScore extends Additive

	// Blessing of Kings
	object StrengthPercent extends Multiplicative
	object AgilityPercent extends Multiplicative
	object IntellectPercent extends Multiplicative

	// Stamina
	object StaminaPercent extends Multiplicative

	// Crit
	object CriticalStrikeAttunement extends Multiplicative
	object CriticalStrikeScore extends Additive
	object CritChancePercent extends Additive

	// Haste
	object HasteAttunement extends Multiplicative
	object HasteScore extends Additive
	object HastePercent extends Multiplicative

	// Mastery
	object MasteryAttunement extends Multiplicative
	object MasteryScore extends Additive
	object MasteryFactor extends Unique[Double](0)

	// Multistrike
	object MultistrikeAttunement extends Multiplicative
	object MultistrikeScore extends Additive
	object MultistrikePercent extends Additive

	// Versatility
	object VersatilityScore extends Additive
	object VersatilityPercent extends Additive

	// Spellpower
	object SpellPowerPercent extends Multiplicative

	// Mana pool
	object ManaPoolPercent extends Multiplicative
	object InCombatRegen extends Maximum[Double](0)
}
