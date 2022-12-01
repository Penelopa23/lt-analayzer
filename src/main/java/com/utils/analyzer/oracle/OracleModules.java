package com.utils.analyzer.oracle;


import com.utils.analyzer.Types;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OracleModules {
    FIRSTSERVICE("First Service", "FS", Types.FIRSTSERVICEFORORACLE),
    SECONDSERVICE("Second Service", "SS", Types.SECONDSERVICEFORORACLE),
    THIRDSERVICE("Third Service", "THS", Types.THIRDSERVICEFORORACLE);

    private final String moduleName;
    private final String tablePrefix;
    private final Types type;
}
