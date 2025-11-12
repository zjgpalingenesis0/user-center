package com.zjg.usercenter;

import com.zjg.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AlgorithmUtilsTest {

    @Test
    void test() {
        String word1 = "晚风是狗";
        String word2 = "晚风不是狗";
        String word3 = "晚风是风不是狗";
        int score1 = AlgorithmUtils.minDistance(word1, word2);   //1
        int score2 = AlgorithmUtils.minDistance(word1, word3);  //3
        System.out.println(score1);
        System.out.println(score2);
    }

    @Test
    void testCompareTags() {
        List<String> tagsList1 = Arrays.asList("java", "大一", "男");
        List<String> tagsList2 = Arrays.asList("java", "大二", "男", "python");
        List<String> tagsList3 = Arrays.asList("python", "大一", "女", "java");

        int score1 = AlgorithmUtils.minDistance(tagsList1, tagsList2);
        int score2 = AlgorithmUtils.minDistance(tagsList1, tagsList3);
        System.out.println(score1);
        System.out.println(score2);
    }

    @Test
    void testCompareTagsNoSeq() {
        List<String> tagsList1 = Arrays.asList("java", "大一", "男");
        List<String> tagsList2 = Arrays.asList("java", "大二", "男", "python");
        List<String> tagsList3 = Arrays.asList("python", "大一", "女", "c++");

        int score1 = AlgorithmUtils.minDistance(tagsList1, tagsList2);
        int score2 = AlgorithmUtils.minDistance(tagsList1, tagsList3);
        System.out.println(score1);
        System.out.println(score2);
    }
}
