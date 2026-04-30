package com.example.backend.module;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaContractTest {

    private static final Path INIT_SQL = Path.of("init.sql");
    private static final Path ENTITY_DIR = Path.of("src/main/java/com/example/backend/entity");
    private static final Pattern CREATE_TABLE = Pattern.compile("(?im)^CREATE\\s+TABLE\\s+`([^`]+)`");
    private static final Pattern DROP_TABLE = Pattern.compile("(?im)^DROP\\s+TABLE\\s+IF\\s+EXISTS\\s+`([^`]+)`");
    private static final Pattern FOREIGN_KEY_TARGET = Pattern.compile("(?im)REFERENCES\\s+`([^`]+)`\\s*\\(`id`\\)");

    @Test
    void initSqlHasDatabaseGuardsAndReenablesForeignKeys() throws IOException {
        String sql = Files.readString(INIT_SQL);

        assertTrue(sql.contains("CREATE DATABASE IF NOT EXISTS `pet_adoption`"));
        assertTrue(sql.contains("USE `pet_adoption`;"));
        assertTrue(sql.contains("SET FOREIGN_KEY_CHECKS = 0;"));
        assertTrue(sql.trim().endsWith("SET FOREIGN_KEY_CHECKS = 1;"));
    }

    @Test
    void everyEntityHasMatchingCreateTableStatement() throws IOException {
        Set<String> tables = matches(Files.readString(INIT_SQL), CREATE_TABLE);
        Set<String> expectedTables = Files.list(ENTITY_DIR)
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> path.getFileName().toString().replace(".java", ""))
                .filter(name -> !name.startsWith("I"))
                .map(DatabaseSchemaContractTest::camelToSnake)
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> missingTables = new TreeSet<>(expectedTables);
        missingTables.removeAll(tables);

        assertTrue(missingTables.isEmpty(),
                () -> "实体缺少建表语句:\n" + String.join("\n", missingTables));
    }

    @Test
    void dropTableListMatchesCreateTableList() throws IOException {
        String sql = Files.readString(INIT_SQL);
        Set<String> createdTables = matches(sql, CREATE_TABLE);
        Set<String> droppedTables = matches(sql, DROP_TABLE);

        assertEquals(createdTables, droppedTables,
                () -> "DROP TABLE 与 CREATE TABLE 不一致\n缺少 DROP:\n"
                        + difference(createdTables, droppedTables)
                        + "\n多余 DROP:\n"
                        + difference(droppedTables, createdTables));
    }

    @Test
    void everyForeignKeyTargetTableExists() throws IOException {
        String sql = Files.readString(INIT_SQL);
        Set<String> createdTables = matches(sql, CREATE_TABLE);
        Set<String> foreignKeyTargets = matches(sql, FOREIGN_KEY_TARGET);
        Set<String> missingTargets = new TreeSet<>(foreignKeyTargets);
        missingTargets.removeAll(createdTables);

        assertTrue(missingTargets.isEmpty(),
                () -> "外键引用了不存在的表:\n" + String.join("\n", missingTargets));
    }

    @Test
    void deleteJobLifecycleTimestampsAreNullableUntilJobRuns() throws IOException {
        String sql = Files.readString(INIT_SQL);

        assertTrue(sql.contains("`start_time` datetime COMMENT '开始时间'"));
        assertTrue(sql.contains("`finish_time` datetime COMMENT '结束时间'"));
    }

    private static Set<String> matches(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        Set<String> values = new TreeSet<>();
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    private static String difference(Set<String> left, Set<String> right) {
        Set<String> result = new TreeSet<>(left);
        result.removeAll(right);
        return result.isEmpty() ? "(无)" : String.join("\n", result);
    }

    private static String camelToSnake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
}
