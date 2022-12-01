package com.utils.analyzer.postgre;

import com.utils.analyzer.utils.Types;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostgreModules {

    FIRSTSERVICE("First Service", "FS",Types.FIRSTSERVICEFORPOSTGRE),
    SECONDSERVICE("Second Service", "SS", Types.SECONDSERVICEFORPOSTGRE),
    THIRDSERVICE("Third Service", "THS", Types.THIRDSERVICEFORPOSTGRE);

    private final String moduleName;
    private final String tablePrefix;
    private final Types type;
}
