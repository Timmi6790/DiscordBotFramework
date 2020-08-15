package de.timmi6790.discord_framework.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ReflectionUtilitiesTest {

    @Test
    void deepCopyTest() {
        final List<Integer> testList = new ArrayList<>();
        testList.add(41);
        testList.add(93);

        final Map<String, String> testMap = new HashMap<>();
        testMap.put("Key", "Value");
        testMap.put("Key1", "Value1");

        final TestCopy originalObject = new TestCopy(
                "test",
                42,
                42L,
                testList,
                testMap
        );

        final TestCopy copiedObject = ReflectionUtilities.deepCopy(originalObject);
        assertThat(originalObject).isEqualTo(copiedObject);

        copiedObject.setTestString("asdadasdasdsadasads");
        assertThat(copiedObject.getTestString()).isNotEqualTo(originalObject.getTestString());

        copiedObject.setTestInt(900);
        assertThat(copiedObject.getTestInt()).isNotEqualTo(originalObject.getTestInt());

        copiedObject.setTestLong(90L);
        assertThat(copiedObject.getTestLong()).isNotEqualTo(originalObject.getTestLong());

        copiedObject.setTestList(new ArrayList<>());
        assertThat(copiedObject.getTestList()).isNotEqualTo(originalObject.getTestList());

        copiedObject.setTestMap(new HashMap<>());
        assertThat(copiedObject.getTestMap()).isNotEqualTo(originalObject.getTestMap());

        assertThat(copiedObject).isNotEqualTo(originalObject);
    }

    @Data
    @AllArgsConstructor
    private static class TestCopy {
        private String testString;
        private int testInt;
        private Long testLong;
        private List<Integer> testList;
        private Map<String, String> testMap;
    }
}