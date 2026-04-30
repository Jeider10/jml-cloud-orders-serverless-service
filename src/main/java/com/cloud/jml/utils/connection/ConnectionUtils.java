package com.cloud.jml.utils.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Getter
@Setter
@Component
public class ConnectionUtils {

    private final ConnectionPropertiesUtils connectionPropertiesUtils;

    public ConnectionUtils(ConnectionPropertiesUtils connectionPropertiesUtils) {
        this.connectionPropertiesUtils = connectionPropertiesUtils;
        log.info("🔥 ConnectionUtils inicializado correctamente.");
    }

    /**
     * 🔧 Construye un HikariDataSource configurado con los parametros indicados.
     * - Incluye validacion inmediata de conexion al crear el pool.
     * - Si la conexion falla, lanza una excepcion al arrancar (fail-fast).
     */
    public DataSource buildDataSource(String url, String username, String password) {
        log.info("🧱 Creando HikariDataSource con los parametros proporcionados...");

        // 🧩 Configuracion base de HikariCP
        HikariConfig config = getHikariConfig(url, username, password);

        // 🔹 Crear el DataSource con la configuracion anterior
        HikariDataSource dataSource = new HikariDataSource(config);

        // ✅ Validar inmediatamente la conexion inicial
        try (Connection connection = dataSource.getConnection()) {
            log.info("✅ Conexion inicial validada correctamente a: {}", connection.getMetaData().getURL());
        } catch (SQLException e) {
            log.error("❌ Error al validar la conexion inicial a la base de datos: {}", e.getMessage());
            throw new RuntimeException("Error al inicializar la conexion a la base de datos", e);
        }

        log.info("✅ HikariDataSource creado y validado exitosamente.");

        return dataSource;
    }

    public @NotNull HikariConfig getHikariConfig(String url, String username, String password) {
        log.info("🧱 Creando HikariConfig con los parametros proporcionados...");

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(connectionPropertiesUtils.getDriverClassName());

        // 🔥 OPTIMIZACIONES PARA MYSQL (MEJORAN RENDIMIENTO)
        // 🔸 Cachea los PreparedStatements (reduce uso de CPU y mejora velocidad)
        config.addDataSourceProperty("cachePrepStmts", "true");
        // 🔸 Tamano del cache de statements
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        // 🔸 Longitud maxima de SQL que se cachea
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        // 🔸 Usa prepared statements del servidor
        config.addDataSourceProperty("useServerPrepStmts", "true");
        // 🔸 Mejora manejo de sesion local
        config.addDataSourceProperty("useLocalSessionState", "true");
        // 🔸 Optimiza inserts/batch (MUY importante para rendimiento)
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        // 🔸 Cachea metadata de resultados
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        // 🔸 Cachea configuracion del servidor
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        // 🔸 Reduce operaciones innecesarias de autocommit
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        // 🔸 Desactiva metricas innecesarias (mejora rendimiento)
        config.addDataSourceProperty("maintainTimeStats", "false");
        // 🔥 TIMEOUT DE VALIDACION (mejor control de conexiones)
        config.setValidationTimeout(5000);

        // ⚙️ Pool de conexiones
        // 🔸 Define el numero maximo de conexiones simultaneas que puede abrir el pool.
        config.setMaximumPoolSize(10);
        // 🔸 Numero minimo de conexiones que el pool mantiene abiertas incluso si no hay trafico.
        config.setMinimumIdle(2);
        // 🔸 Tiempo (en milisegundos) que una conexion inactiva puede permanecer abierta antes de cerrarse.
        config.setIdleTimeout(30000);
        // 🔥 Tiempo maximo para obtener una conexion del pool
        config.setConnectionTimeout(30000); // 5 min
        // 🔥 Tiempo maximo de vida de una conexion (evita conexiones zombie)
        config.setMaxLifetime(1800000); // 30 min
        // 🔥 Mantiene viva la conexion (evita que MySQL la mate)
        config.setKeepaliveTime(300000); // 5 min
        // 🔸 Define si cada operacion SQL se confirma automaticamente; util para evitar inconsistencias fuera de transacciones gestionadas por Hibernate.
        config.setAutoCommit(true);
        // 🔸 Nombre personalizado del pool de conexiones (solo para logs y monitoreo).
        config.setPoolName("JML-HikariPool");

        // 🧠 Configuracion adicional para robustez
        // 🔸 Si la conexion inicial falla, lanza error inmediatamente (fail-fast)
        config.setInitializationFailTimeout(0);
        // 🔸 Query simple para validar las conexiones creadas
        config.setConnectionTestQuery("SELECT 1");

        log.info("✅ HikariConfig creado exitosamente.");

        return config;
    }
}
