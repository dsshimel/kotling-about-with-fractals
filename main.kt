import kotlin.random.Random

fun main(/*args: Array<String>*/) {
  // println("Hello world!")
  var x = Random.Default.nextInt()
  // println(String.format("Your random number is %i", x))
  // println("Your random number is " + x)
  x = 2
  // when (x) {
    // 0 ->  println("0")
    // 1, 2 -> {
    //   println("x is ")
    //   println("1 or 2")
    // }
    // else -> println("x is something else")
  // }

  val g = Generator(arrayOf(Point(0.0, -0.5), Point(0.5, 0.5), Point(1.0, -0.5)))
  // println(g.getSegments())
  // val f = LinearFractal(arrayOf(Point(0.0, -0.5), Point(1.0, 0.5)))
  val f = LinearFractal(arrayOf(Point(0.0, -0.5), Point(0.5, 0.5), Point(1.0, -0.5)))
  // println(f.getSegments())

  // val nextF = f.generateNextLinearFractalLayerXBiased(g)
  // println(nextF.getSegments())

  // println("nextF")
  val nextF = f.generateFractalLayerByRescalingGenerator(g)
  println("Fractal segments")
  println(nextF.getSegments())

  // println("nextNextF")
  // val nextNextF = nextF.generateFractalLayerByRescalingGenerator(g)
  // println(nextNextF.getSegments())
}

data class Point(var x: Double, var y: Double) {
  init {
    if (x < 0 || x > 1) {
      throw Exception("x must be between 0 and 1")
    }
    if (y < -0.5 || y > 0.5) {
      throw Exception("y must be between -0.5 and 0.5")
    }
  }
  override fun toString(): String {
    return "(" + this.x + ", " + this.y + ")"
  }
}

class Segment(val start: Point, val end: Point) {
  override fun toString(): String = "[" + this.start + ", " + this.end + "]"
}

class Generator(
  val points: Array<Point> = arrayOf(Point(0.0, 0.0), Point(1.0, 0.0),)) {

    init {
      if (points.size < 2) {
        throw Exception("There must be at least two points in the Generator")
      }

      // normalizePoints()
    }

    fun getSegments(): ArrayList<Segment> {
      val segments = ArrayList<Segment>()
      for (i in 0..this.points.size - 2) {
        segments.add(Segment(this.points[i], this.points[i + 1]))
      }
      return segments
    }

    fun copyPoints(): MutableList<Point> {
      return (this.points.map { it.copy() }).toMutableList()
    }

    // fun normalizePoints() {
    //   // Rescale the generator to be between [0.0, 1.0] on the x axis and
    //   // [-0.5, 0.5] on the y axis.
    //   val deltaX = points.last().x - points[0].x

    //   var maxX, minX = 0.0, 1.0
    //   var maxY, minY = -0.5, 0.5
    //   for (point in points) {
    //     if (point.x < minX) minX = point.x
    //     if (point.x > maxX) maxX = point.x

    //     if (point.y < minY) minY = point.y
    //     if (point.y > maxY) maxY = point.y
    //   }
    //   val deltaX = maxX - minX
    //   val deltaY = maxY - minY

    //   for (point in points) {
    //     point.x = (point.x - minX) / deltaX
    //     point.y = (point.y + 0.5)
    //   }
    // }
} 

// A LinearFractal also represents the Initiator in a fractal sequence
// because the Initiator is the root of a fractal tree
class LinearFractal(
  val points: Array<Point> = arrayOf(Point(0.0, 0.0), Point(1.0, 0.0),),
  var childFractal: LinearFractal? = null) {

    init {
      print("Fractal points: ")
      for (point in points) {
        print("" + point + ", ")
      }
      println()
    }

    fun getSegments(): ArrayList<Segment> {
      val segments = ArrayList<Segment>()
      for (i in 0..this.points.size - 2) {
        segments.add(Segment(this.points[i], this.points[i + 1]))
      }
      return segments
    }

    fun generateFractalLayerByRescalingGenerator(generator: Generator): LinearFractal {
      // For each segment of the fractal, we'll create a rescaled copy of the 
      // generator that follows the length and slope of the segment. It is reshaped
      // to fit inside the rectangle defined by the two Points in the fractal
      // segment.
      val nextFractalLayerPoints = ArrayList<Point>()
      // for ((index, fractalSegment) in this.getSegments().withIndex()) {
      for (fractalSegment in this.getSegments()) {
        // Transform the array of Points representing the generator into an array
        // of points defining a segment of the next layer of the fractal.
        // F(G) = S or G * F = S, * meaning transformation or convolution of a sort.
        val nextFractalSegmentPoints = generator.copyPoints()
        print("Generator points for initial next fractal layer points: ")
        for (point in nextFractalSegmentPoints) {
          print("" + point + ", ")
        }
        println()

        val deltaXFractal = fractalSegment.end.x - fractalSegment.start.x
        if (deltaXFractal <= 0) {
          // The segment is infinitely narrow, so we can't fractallize it.
          continue
        }
        val xDisplacementFractal = fractalSegment.start.x
        // Ranges between [-1.0, 1.0] because the max point Y value is 0.5 in either direction.
        val deltaYFractal = fractalSegment.end.y - fractalSegment.start.y
        val yDisplacementFractal = (fractalSegment.end.y + fractalSegment.start.y) / 2
        for (point in nextFractalSegmentPoints) {
          point.x = (deltaXFractal * point.x) + xDisplacementFractal
          point.y = (deltaYFractal * point.y) + yDisplacementFractal
        }

        // If we're not on the last segment in the fractal, throw out the last point of the
        // new segment
        // Actually I don't think I want this
        // if (index != this.getSegments().size - 1) {
        //   nextFractalSegmentPoints.removeAt(nextFractalSegmentPoints.lastIndex)
        // }
        nextFractalLayerPoints.addAll(nextFractalSegmentPoints)
      }

      val nextFractalLayer = LinearFractal(points = nextFractalLayerPoints.toTypedArray())
      this.childFractal = nextFractalLayer
      return nextFractalLayer
    }

    fun generateNextLinearFractalLayerXBiased(generator: Generator): LinearFractal {
      var previousPoint = this.points[0]
      val nextPoints = ArrayList<Point>()
      nextPoints.add(previousPoint)
      for (fractalSegment in this.getSegments()) {
        for (generatorSegment in generator.getSegments()) {
          // Since the generator's x values range between 0 and 1, the x delta in a // generator segment represents the fraction of the fractal segment's x
          // delta that should be added to the previous point to create the next 
          // point 
          val generatorDeltaX = generatorSegment.end.x - generatorSegment.start.x
          val fractalDeltaX = fractalSegment.end.x - fractalSegment.start.x
          val xDisplacement = (generatorDeltaX * fractalDeltaX)
          val xNext = xDisplacement + previousPoint.x

          val generatorDeltaY = generatorSegment.end.y - generatorSegment.start.y
          val generatorSlope = generatorDeltaY / generatorDeltaX
          val yDisplacement = generatorSlope * xDisplacement
          val yNext = yDisplacement + previousPoint.y

          val nextPoint = Point(xNext, yNext)
          nextPoints.add(nextPoint)
          previousPoint = nextPoint
        }
      }

      val nextFractal = LinearFractal(points = nextPoints.toTypedArray())
      this.childFractal = nextFractal
      return nextFractal
    }
}