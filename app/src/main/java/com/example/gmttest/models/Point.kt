package com.example.gmttest.models

class Point(var x: Float, var y: Float)
{
    override fun equals(other: Any?): Boolean {
        if (other !is Point) return false
        return x == other.x && y == other.y
    }

    operator fun plus(other: Point): Point {
        val newX = this.x + other.x
        val newY = this.y + other.y
        return Point(newX, newY)
    }

    operator fun minus(other: Point): Point {
        val newX = this.x - other.x
        val newY = this.y - other.y
        return Point(newX, newY)
    }

    operator fun times(other: Point): Point {
        val newX = this.x * other.x
        val newY = this.y * other.y
        return Point(newX, newY)
    }

    operator fun div(other: Point): Point {
        val newX = this.x / other.x
        val newY = this.y / other.y
        return Point(newX, newY)
    }

    operator fun plus(other: Float): Point {
        val newX = this.x + other
        val newY = this.y + other
        return Point(newX, newY)
    }

    operator fun minus(other: Float): Point {
        val newX = this.x - other
        val newY = this.y - other
        return Point(newX, newY)
    }

    operator fun times(other: Float): Point {
        val newX = this.x * other
        val newY = this.y * other
        return Point(newX, newY)
    }

    operator fun div(other: Float): Point {
        val newX = this.x / other
        val newY = this.y / other
        return Point(newX, newY)
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}