package holysim.paladin

import holysim.engine.{Aura, Proc}

trait PaladinProcs { this: Paladin =>
	object procs {
		/**
		 * Casting Holy Radiance causes your next Holy Shock and Holy Shock multistrikes to also heal up to 6 allies
		 * within 10 yards of the target for 15% of the original healing done. Can accumulate up to 2 charges.
		 *
		 * Improved Daybreak (Level 92+)
		 * Increases the healing from Daybreak by 100%.
		 */
		object Daybreak extends Proc("Daybreak") {
			/**
			 * Your next Holy Shock will heal each ally within 10 yards of the target for 15% of the original healing done.
			 */
			object DaybreakAura extends Aura("Daybreak") with Aura.Duration with Aura.Stackable {
				val duration = 10000
				val max_stacks = 2
			}
		}
	}
}
