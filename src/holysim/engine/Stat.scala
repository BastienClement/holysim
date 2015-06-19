package holysim.engine

import scala.language.implicitConversions
import holysim.engine.PlayerStats.VersatilityFactors
import holysim.utils.Reactive

sealed trait Stat {
	def apply(value: Int) = StatValue(this, value)
}

case class StatValue(stat: Stat, value: Int)

object Stat {
	object Stamina extends Stat
	object Intellect extends Stat
	object CriticalStrike extends Stat
	object Haste extends Stat
	object Mastery extends Stat
	object Multistrike extends Stat
	object Versatility extends Stat
	object Spellpower extends Stat
	object Spirit extends Stat
	object Avoidance extends Stat
	object Leech extends Stat
	object Speed extends Stat

	implicit class Impl(value: Int) {
		def stamina = Stamina(value)
		def intellect = Intellect(value)
		def crit = CriticalStrike(value)
		def haste = Haste(value)
		def mastery = Mastery(value)
		def multistrike = Multistrike(value)
		def versatility = Versatility(value)
		def spirit = Spirit(value)
		def spellpower = Spellpower(value)
		def avoidance = Avoidance(value)
		def leech = Leech(value)
		def speed = Speed(value)
	}
}

object PlayerStats {
	case class VersatilityFactors(out: Double, in: Double)
}

trait PlayerStats {
	this: Actor =>

	// Automatically convert stats and modifiers identifiers to actual values
	private implicit def statToValue(stat: Stat): Int = gear(stat)
	private implicit def modifierToValue[T](mod: Modifier[T]): T = modifier(mod)

	// Implicit conversion
	private implicit def doubleToInt(d: Double): Int = d.toInt
	private implicit def longToInt(l: Long): Int = l.toInt

	val intellect = Reactive[Int] {
		(Mod.BaseIntellect + Stat.Intellect + Mod.IntellectScore) * Mod.IntellectPercent
	}

	val stamina = Reactive[Int] {
		(Mod.BaseStamina + Stat.Stamina + Mod.StaminaScore) * Mod.StaminaPercent
	}

	val max_health = Reactive[Int] {
		val health_per_stamina = 60
		stamina * health_per_stamina
	}

	val criticalstrike_score = Reactive[Int] {
		((Stat.CriticalStrike + Mod.CriticalStrikeScore) * Mod.CriticalStrikeAttunement).round
	}

	val criticalstrike = Reactive[Double] {
		val crit_base = 5
		val crit_cost = 110.0
		(crit_base + criticalstrike_score / crit_cost + Mod.CritChancePercent) / 100
	}

	val haste_score = Reactive[Int] {
		((Stat.Haste + Mod.HasteScore) * Mod.HasteAttunement).round
	}

	val haste = Reactive[Double] {
		val haste_cost = 90.0
		(100 + (haste_score / haste_cost)) * Mod.HastePercent / 100
	}

	val mastery_score = Reactive[Int] {
		((Stat.Mastery + Mod.MasteryScore) * Mod.MasteryAttunement).round
	}

	val mastery = Reactive[Double] {
		val unit_cost = 110.0
		val unit_base = 8
		(mastery_score / unit_cost + unit_base) * Mod.MasteryFactor / 100
	}

	val spirit = Reactive[Int] {
		Mod.BaseSpirit + Stat.Spirit + Mod.SpiritScore
	}

	val multistrike_score = Reactive[Int] {
		((Stat.Multistrike + Mod.MultistrikeScore) * Mod.MultistrikeAttunement).round
	}

	val multistrike = Reactive[Double] {
		val ms_cost = 66.0
		(multistrike_score / ms_cost + Mod.MultistrikePercent) / 100
	}

	val versatility_score = Reactive[Int] {
		Stat.Versatility + Mod.VersatilityScore
	}

	val versatility = Reactive[VersatilityFactors] {
		val versatility_cost = 130.0
		val out_factor = versatility_score / versatility_cost + Mod.VersatilityPercent
		val in_factor = out_factor / 2
		VersatilityFactors(out_factor, in_factor)
	}

	val spellpower = Reactive[Int] {
		(Stat.Spellpower + intellect) * Mod.SpellPowerPercent
	}
}
