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
    @Query(value = """
        SELECT DISTINCT a.* FROM animal a
        JOIN usuario u ON a.usuario_id = u.usuario_id
        LEFT JOIN animal_vacuna av ON av.animal_id = a.animal_id
        LEFT JOIN vacuna v ON v.vacuna_id = av.vacuna_id
        WHERE a.estatus = 'DISPONIBLE'
          AND a.inapropiado = false
          AND (CAST(:especie AS VARCHAR) IS NULL OR UPPER(a.especie) = UPPER(CAST(:especie AS VARCHAR)))
          AND (CAST(:sexo AS VARCHAR) IS NULL OR a.sexo::text = CAST(:sexo AS VARCHAR))
          AND (:esterilizado IS NULL OR a.esterilizado = :esterilizado)
          AND (CAST(:codigoPostal AS VARCHAR) IS NULL OR u.codigo_postal = CAST(:codigoPostal AS VARCHAR))
          AND (CAST(:vacunaNombre AS VARCHAR) IS NULL OR (v.nombre IS NOT NULL AND LOWER(v.nombre) = LOWER(CAST(:vacunaNombre AS VARCHAR))))
          AND (CAST(:razaId AS UUID) IS NULL OR a.raza_id = CAST(:razaId AS UUID))
          AND (:sinPadecimientos = false OR NOT EXISTS (
              SELECT 1 FROM animal_padecimiento ap WHERE ap.animal_id = a.animal_id
          ))
    """, nativeQuery = true)
    fun buscarConFiltros(
        @Param("especie") especie: String?,
        @Param("sexo") sexo: String?,
        @Param("esterilizado") esterilizado: Boolean?,
        @Param("codigoPostal") codigoPostal: String?,
        @Param("vacunaNombre") vacunaNombre: String?,
        @Param("razaId") razaId: UUID?,
        @Param("sinPadecimientos") sinPadecimientos: Boolean
    ): List<AnimalEntity>
}
