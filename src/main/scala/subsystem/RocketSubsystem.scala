// See LICENSE.SiFive for license details.

package freechips.rocketchip.subsystem

import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockNode, ClockTempNode, ResetStretcher}
import freechips.rocketchip.tile._

case class RocketCrossingParams(
  crossingType: ClockCrossingType = SynchronousCrossing(),
  master: TilePortParamsLike = TileMasterPortParams(),
  slave: TileSlavePortParams = TileSlavePortParams(),
  mmioBaseAddressPrefixWhere: TLBusWrapperLocation = CBUS,
  stretchResetCycles: Option[Int] = None
) extends TileCrossingParamsLike {
  def injectClockNode(context: Attachable)(implicit p: Parameters): ClockNode = {
    if (stretchResetCycles.isDefined) {
      val rs = LazyModule(new ResetStretcher(stretchResetCycles.get))
      rs.node
    } else {
      ClockTempNode()
    }
  }
  def forceSeparateClockReset: Boolean = false
}

case class RocketTileAttachParams(
  tileParams: RocketTileParams,
  crossingParams: RocketCrossingParams,
  lookup: LookupByHartIdImpl
) extends CanAttachTile { type TileType = RocketTile }

case object RocketTilesKey extends Field[Seq[RocketTileParams]](Nil)
case object RocketCrossingKey extends Field[Seq[RocketCrossingParams]](List(RocketCrossingParams()))

trait HasRocketTiles extends HasTiles { this: BaseSubsystem =>
  // this line actullay do a lot of things include to init the
  // tile from config, balabala
  // tiles is from InstantiatesTiles which is the HasTiles trait
  // to get the tiles, we need firstly init it, balabala
  val rocketTiles = tiles.collect { case r: RocketTile => r }

  def coreMonitorBundles = (rocketTiles map { t =>
    t.module.core.rocketImpl.coreMonitorBundle
  }).toList
}

// BaseSubsystem is the bus architecture of the chip
// HasRocketTiles is just the tile which connect to the bus
// combine both to create the basic of CPU
class RocketSubsystem(implicit p: Parameters) extends BaseSubsystem with HasRocketTiles {
  // this statement is redundant if the ExampleRocketSystem already override it
  override lazy val module = new RocketSubsystemModuleImp(this)
}

// debug module is connect to HasTilesModuleImp
class RocketSubsystemModuleImp[+L <: RocketSubsystem](_outer: L) extends BaseSubsystemModuleImp(_outer)
    with HasTilesModuleImp
