package holysim.engine

abstract class Dummy(val name: String) extends Actor {

}

class TankDummy(i: Int) extends Dummy(s"Tank #$i") {
	val role = Actor.Role.Tank
}

class HealerDummy(i: Int) extends Dummy(s"Healer #$i") {
	val role = Actor.Role.Healer
}

class DamageDummy(i: Int) extends Dummy(s"Damager #$i") {
	val role = Actor.Role.Damage
}



