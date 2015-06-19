package holysim.paladin

import holysim.engine.{Actor, PlayerStats}

class Paladin extends Actor with PlayerStats with PaladinAuras with PaladinSpells {
	// Apply passive auras
	this gain BaseStats
	this gain PlateSpecialization
	this gain SanctifiedLight
	this gain HolyInsight
	this gain InfusionOfLight
	this gain IlluminatedHealing
}
