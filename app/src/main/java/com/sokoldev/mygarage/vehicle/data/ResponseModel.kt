package com.sokoldev.mygarage.vehicle.data


data class ResponseModel(
    val make: Make,
    val model: Model,
    val engine: Engine,
    val years: List<Year>,
    val colors: List<Color>
)

data class Color (
    val category: String,
    val options: List<Option>
)

data class Option (
    val id: String,
    val name: String,
    val equipmentType: EquipmentType,
    val availability: Availability
)

enum class Availability {
    Used
}

enum class EquipmentType {
    Color
}

data class Engine (
    val id: String,
    val name: String,
    val equipmentType: String,
    val availability: String,
    val compressionRatio: Double,
    val cylinder: Long,
    val size: Long,
    val displacement: Long,
    val configuration: String,
    val fuelType: String,
    val horsepower: Long,
    val torque: Long,
    val totalValves: Long,
    val type: String,
    val code: String,
    val compressorType: String,
    val rpm: RPM,
    val valve: Valve
)

data class RPM (
    val horsepower: Long,
    val torque: Long
)

data class Valve (
    val timing: String,
    val gear: String
)

data class Make (
    val id: Long,
    val name: String,
    val niceName: String
)

data class Model (
    val id: String,
    val name: String,
    val niceName: String
)

data class Year (
    val id: Long,
    val year: Long,
    val styles: List<Style>
)

data class Style (
    val id: Long,
    val name: String,
    val submodel: Submodel,
    val trim: String
)

data class Submodel (
    val body: String,
    val modelName: String,
    val niceName: String
)
