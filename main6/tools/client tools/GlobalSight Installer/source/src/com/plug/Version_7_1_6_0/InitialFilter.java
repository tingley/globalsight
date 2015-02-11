package com.plug.Version_7_1_6_0;

import java.sql.SQLException;
import java.util.List;

public interface InitialFilter
{
    String insert() throws SQLException;
    void updateFilterIds(List list) throws SQLException;
    List getValuesToUpdate() throws SQLException;
}
