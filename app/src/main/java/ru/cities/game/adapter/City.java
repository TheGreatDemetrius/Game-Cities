package ru.cities.game.adapter;

import ru.cities.game.R;

public class City {
    private final static int[] flags = {R.drawable.ic_0, R.drawable.ic_1, R.drawable.ic_2,
            R.drawable.ic_3, R.drawable.ic_4, R.drawable.ic_5, R.drawable.ic_6,
            R.drawable.ic_7, R.drawable.ic_8, R.drawable.ic_9, R.drawable.ic_10,
            R.drawable.ic_11, R.drawable.ic_12, R.drawable.ic_13, R.drawable.ic_14,
            R.drawable.ic_15, R.drawable.ic_16, R.drawable.ic_17, R.drawable.ic_18,
            R.drawable.ic_19, R.drawable.ic_20, R.drawable.ic_21, R.drawable.ic_22,
            R.drawable.ic_23, R.drawable.ic_24, R.drawable.ic_25, R.drawable.ic_26,
            R.drawable.ic_27, R.drawable.ic_28, R.drawable.ic_29, R.drawable.ic_30,
            R.drawable.ic_31, R.drawable.ic_32, R.drawable.ic_33, R.drawable.ic_34,
            R.drawable.ic_35, R.drawable.ic_36, R.drawable.ic_37, R.drawable.ic_38,
            R.drawable.ic_39, R.drawable.ic_40, R.drawable.ic_41, R.drawable.ic_42,
            R.drawable.ic_43, R.drawable.ic_44, R.drawable.ic_45, R.drawable.ic_46,
            R.drawable.ic_47, R.drawable.ic_48, R.drawable.ic_49, R.drawable.ic_50,
            R.drawable.ic_51, R.drawable.ic_52, R.drawable.ic_53, R.drawable.ic_54,
            R.drawable.ic_55, R.drawable.ic_56, R.drawable.ic_57, R.drawable.ic_58,
            R.drawable.ic_59, R.drawable.ic_60, R.drawable.ic_61, R.drawable.ic_62,
            R.drawable.ic_63, R.drawable.ic_64, R.drawable.ic_65, R.drawable.ic_66,
            R.drawable.ic_67, R.drawable.ic_68, R.drawable.ic_69, R.drawable.ic_70,
            R.drawable.ic_71, R.drawable.ic_72, R.drawable.ic_73, R.drawable.ic_74,
            R.drawable.ic_75, R.drawable.ic_76, R.drawable.ic_77, R.drawable.ic_78,
            R.drawable.ic_79, R.drawable.ic_80, R.drawable.ic_81, R.drawable.ic_82,
            R.drawable.ic_83, R.drawable.ic_84, R.drawable.ic_85, R.drawable.ic_86,
            R.drawable.ic_87, R.drawable.ic_88, R.drawable.ic_89, R.drawable.ic_90,
            R.drawable.ic_91, R.drawable.ic_92, R.drawable.ic_93, R.drawable.ic_94,
            R.drawable.ic_95, R.drawable.ic_96, R.drawable.ic_97, R.drawable.ic_98,
            R.drawable.ic_99, R.drawable.ic_100, R.drawable.ic_101, R.drawable.ic_102,
            R.drawable.ic_103, R.drawable.ic_104, R.drawable.ic_105, R.drawable.ic_106,
            R.drawable.ic_107, R.drawable.ic_108, R.drawable.ic_109, R.drawable.ic_110,
            R.drawable.ic_111, R.drawable.ic_112, R.drawable.ic_113, R.drawable.ic_114,
            R.drawable.ic_115, R.drawable.ic_116, R.drawable.ic_117, R.drawable.ic_118,
            R.drawable.ic_119, R.drawable.ic_120, R.drawable.ic_121, R.drawable.ic_122,
            R.drawable.ic_123, R.drawable.ic_124, R.drawable.ic_125, R.drawable.ic_126,
            R.drawable.ic_127, R.drawable.ic_128, R.drawable.ic_129, R.drawable.ic_130,
            R.drawable.ic_131, R.drawable.ic_132, R.drawable.ic_133, R.drawable.ic_134,
            R.drawable.ic_135, R.drawable.ic_136, R.drawable.ic_137, R.drawable.ic_138,
            R.drawable.ic_139, R.drawable.ic_140, R.drawable.ic_141, R.drawable.ic_142,
            R.drawable.ic_143, R.drawable.ic_144, R.drawable.ic_145, R.drawable.ic_146,
            R.drawable.ic_147, R.drawable.ic_148, R.drawable.ic_149, R.drawable.ic_150,
            R.drawable.ic_151, R.drawable.ic_152, R.drawable.ic_153, R.drawable.ic_154,
            R.drawable.ic_155, R.drawable.ic_156, R.drawable.ic_157, R.drawable.ic_158,
            R.drawable.ic_159, R.drawable.ic_160, R.drawable.ic_161, R.drawable.ic_162,
            R.drawable.ic_163, R.drawable.ic_164, R.drawable.ic_165, R.drawable.ic_166,
            R.drawable.ic_167, R.drawable.ic_168, R.drawable.ic_169, R.drawable.ic_170,
            R.drawable.ic_171, R.drawable.ic_172, R.drawable.ic_173, R.drawable.ic_174,
            R.drawable.ic_175, R.drawable.ic_176, R.drawable.ic_177, R.drawable.ic_178,
            R.drawable.ic_179, R.drawable.ic_180, R.drawable.ic_181, R.drawable.ic_182,
            R.drawable.ic_183, R.drawable.ic_184, R.drawable.ic_185, R.drawable.ic_186,
            R.drawable.ic_187, R.drawable.ic_188, R.drawable.ic_189, R.drawable.ic_190,
            R.drawable.ic_191, R.drawable.ic_192, R.drawable.ic_193};
    private boolean type = true;
    private final int flag;
    private final String name;

    public City(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getFlagResource() {
        return flags[flag];
    }

    public int getId() {
        return flag;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public boolean getType() {
        return type;
    }

    public char getLastChar() {
        char last = name.charAt(name.length() - 1);
        if (last == 'ё' || last == 'ы' || last == 'ь')
            last = name.charAt(name.length() - 2);
        return Character.toUpperCase(last);
    }
}
