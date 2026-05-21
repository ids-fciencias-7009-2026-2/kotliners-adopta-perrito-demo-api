package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalEntity
import com.kotliners.adoptaPerrito.domain.Sexo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/**
 * Repositorio JPA para la entidad AnimalEntity.
 * Extiende JpaRepository para soporte de queries personalizadas con filtros.
 */
interface AnimalRepository : JpaRepository<AnimalEntity, UUID> {

    /** Encuentra todos los animales por el ID del dueno */
    fun findAllByUsuarioId(usuarioId: UUID): Iterable<AnimalEntity>

    /**
     * Busca animales disponibles aplicando filtros opcionales.
     * El join con usuario permite filtrar por codigo postal del cuidador.
     *
     * @param especie        Filtra por especie (PERRO/GATO). Null = todos.
     * @param sexo           Filtra por sexo (MACHO/HEMBRA). Null = todos.
     * @param esterilizado   Filtra por estado de esterilizacion. Null = todos.
     * @param codigoPostal   Filtra por codigo postal del cuidador. Null = todos.
     * @param vacunaNombre   Filtra animales que tienen esta vacuna. Null = todos.
     * @param sinPadecimientos Si true, solo animales sin padecimientos registrados.
     * @return Lista de animales que cumplen todos los filtros activos.
     */
    @Query("""
        SELECT DISTINCT a FROM AnimalEntity a
        JOIN UsuarioEntity u ON a.usuarioId = u.id
        LEFT JOIN AnimalVacunaEntity av ON av.animalId = a.id
        LEFT JOIN VacunaEntity v ON v.id = av.vacunaId
        WHERE a.estatus = 'DISPONIBLE'
          AND a.inapropiado = false
          AND (:especie IS NULL OR UPPER(a.especie) = UPPER(:especie))
          AND (:sexo IS NULL OR a.sexo = :sexo)
          AND (:esterilizado IS NULL OR a.esterilizado = :esterilizado)
          AND (:codigoPostal IS NULL OR u.codigoPostal = :codigoPostal)
          AND (:vacunaNombre IS NULL OR LOWER(v.nombre) = LOWER(:vacunaNombre))
          AND (:sinPadecimientos = false OR NOT EXISTS (
              SELECT 1 FROM AnimalPadecimientoEntity ap WHERE ap.animalId = a.id
          ))
    """)
    fun buscarConFiltros(
        @Param("especie") especie: String?,
        @Param("sexo") sexo: Sexo?,
        @Param("esterilizado") esterilizado: Boolean?,
        @Param("codigoPostal") codigoPostal: String?,
        @Param("vacunaNombre") vacunaNombre: String?,
        @Param("sinPadecimientos") sinPadecimientos: Boolean
    ): List<AnimalEntity>
}
