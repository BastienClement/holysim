package holysim.engine

import scala.collection.mutable
import scala.reflect.ClassTag
import holysim.engine.Mod.{SpellHealingReceivedPercent, SpellHealingPercent}
import holysim.utils.{Reactive, Memoized}
import scala.reflect.runtime.universe._

import scala.math.Numeric
import Modifier._

/**
 * ModifierValue constructor
 */
abstract class Modifier[T](val base: T, val fold: (T, T) => T) {
	def apply(value: Reactive[T]) = ModifierValue(this, value)
}

/**
 * The combined modifier with an associated value
 */
case class ModifierValue[T](mod: Modifier[T], value: Reactive[T])

/**
 * Modifer classes for common pattern
 */
object Modifier {
	abstract class Additive extends Modifier[Int](0, (a, b) => a + b)
	abstract class Multiplicative extends Modifier[Double](1.0, (a, b) => a * b)
	abstract class Maximum[T](base: T)(implicit n: Numeric[T]) extends Modifier[T](base, (a, b) => n.max(a, b))
	abstract class Minimum[T](base: T)(implicit n: Numeric[T]) extends Modifier[T](base, (a, b) => n.min(a, b))
	abstract class Unique[T](base: T) extends Modifier[T](base, (a, b) => b)

	trait Target { this: Actor =>
		/**
		 * The cache of all applied aura effects
		 */
		val modifiers_effects = Memoized { (mod: Modifier[_]) => mutable.Set[Reactive[_]]() }

		/**
		 * Construct the modifier reactive value
		 */
		private def build_modifier[T](mod: Modifier[T]) = Reactive[T] {
			val effects = modifiers_effects(mod).toList.asInstanceOf[List[Reactive[T]]]
			effects.map(_.value).fold(mod.base)(mod.fold)
		}

		/**
		 * The cache of all modifiers created for this actor
		 */
		private val modifiers = Memoized((mod: Modifier[_]) => build_modifier(mod))

		/**
		 * Get a specific modifier reactive value
		 */
		def modifier[T](mod: Modifier[T]): Reactive[T] = modifiers(mod).asInstanceOf[Reactive[T]]
	}
}

/**
 * Implemented modifiers
 */
object Mod {
	// Base stats
	case object BaseIntellect extends Additive
	case object BaseStamina extends Additive
	case object BaseSpirit extends Additive

	case object IntellectScore extends Additive
	case object StaminaScore extends Additive
	case object SpiritScore extends Additive

	// Blessing of Kings
	case object StrengthPercent extends Multiplicative
	case object AgilityPercent extends Multiplicative
	case object IntellectPercent extends Multiplicative

	// Stamina
	case object StaminaPercent extends Multiplicative

	// Crit
	case object CriticalStrikeAttunement extends Multiplicative
	case object CriticalStrikeScore extends Additive
	case object CritChancePercent extends Additive

	// Haste
	case object HasteAttunement extends Multiplicative
	case object HasteScore extends Additive
	case object HastePercent extends Multiplicative

	// Mastery
	case object MasteryAttunement extends Multiplicative
	case object MasteryScore extends Additive
	case object MasteryFactor extends Unique[Double](0)

	// Multistrike
	case object MultistrikeAttunement extends Multiplicative
	case object MultistrikeScore extends Additive
	case object MultistrikePercent extends Additive

	// Versatility
	case object VersatilityScore extends Additive
	case object VersatilityPercent extends Additive

	// Spellpower
	case object SpellPowerPercent extends Multiplicative

	// Mana pool
	case object ManaPoolPercent extends Multiplicative
	case object InCombatRegenPercent extends Maximum[Double](0)

	// Healing
	case object HealingPercent extends Multiplicative
	case class SpellHealingPercent(spell_identity: BoundSymbol[Spell]) extends Multiplicative
	case class SpellHealingReceivedPercent(spell_identity: BoundSymbol[Spell], source: Actor) extends Multiplicative

	// Damage
	case object DamageTakenPercent extends Multiplicative
}
