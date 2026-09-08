package com.vpm.Accounts.config;

import java.sql.Types;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.generator.EventType;
import org.hibernate.id.insert.GetGeneratedKeysDelegate;
import org.hibernate.persister.entity.EntityPersister;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    public SQLiteDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        return switch (sqlTypeCode) {
            case Types.BIT, Types.BOOLEAN -> "boolean";
            case Types.TINYINT -> "tinyint";
            case Types.SMALLINT -> "smallint";
            case Types.INTEGER -> "integer";
            case Types.BIGINT -> "bigint";
            case Types.FLOAT -> "float";
            case Types.REAL -> "real";
            case Types.DOUBLE -> "double";
            case Types.NUMERIC -> "numeric($p,$s)";
            case Types.DECIMAL -> "decimal($p,$s)";
            case Types.CHAR -> "char($l)";
            case Types.VARCHAR -> "varchar($l)";
            case Types.DATE -> "date";
            case Types.TIME -> "time";
            case Types.TIMESTAMP -> "datetime";
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "blob";
            case Types.CLOB -> "clob";
            default -> super.columnType(sqlTypeCode);
        };
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getAddForeignKeyConstraintString(String cn, String[] fk, String t, String[] pk, boolean rpk) {
        return "";
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    @Override
    public boolean supportsUnionAll() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return SQLiteIdentityColumnSupport.INSTANCE;
    }

    private static final class SQLiteIdentityColumnSupport implements IdentityColumnSupport {
        private static final SQLiteIdentityColumnSupport INSTANCE = new SQLiteIdentityColumnSupport();

        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public boolean supportsInsertSelectIdentity() {
            return false;
        }

        @Override
        public boolean hasDataTypeInIdentityColumn() {
            return true;
        }

        @Override
        public String appendIdentitySelectToInsert(String insertSQL) {
            return insertSQL;
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer primary key autoincrement";
        }

        @Override
        public String getIdentityInsertString() {
            return "null";
        }

        @Override
        public GetGeneratedKeysDelegate buildGetGeneratedKeysDelegate(EntityPersister persister) {
            return new GetGeneratedKeysDelegate(persister, true, EventType.INSERT);
        }
    }
}
