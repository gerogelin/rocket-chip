// See LICENSE.SiFive for license details.

package freechips.rocketchip.system

import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util.DontTouch

// parameters is actually config by the command line config
/** Example Top with periphery devices and ports, and a Rocket subsystem */
class ExampleRocketSystem(implicit p: Parameters) extends RocketSubsystem
    with HasAsyncExtInterrupts
    with CanHaveMasterAXI4MemPort
    with CanHaveMasterAXI4MMIOPort
    with CanHaveSlaveAXI4Port
{
  // optionally add ROM devices
  // Note that setting BootROMLocated will override the reset_vector for all tiles
  val bootROM  = p(BootROMLocated(location)).map { BootROM.attach(_, this, CBUS) }
  val maskROMs = p(MaskROMLocated(location)).map { MaskROM.attach(_, this, CBUS) }

  // this module is inherit from LazyModule which has been override through the hierarchy classes
  override lazy val module = new ExampleRocketSystemModuleImp(this)
}

// RocketSubsystemModuleImp is actually a child of LazyModuleImp which has the instantiate to gerenrate code
class ExampleRocketSystemModuleImp[+L <: ExampleRocketSystem](_outer: L) extends RocketSubsystemModuleImp(_outer)
    with HasRTCModuleImp
    with HasExtInterruptsModuleImp
    with DontTouch
