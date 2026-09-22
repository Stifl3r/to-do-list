package za.co.learnings.todolist.api.controller.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PagingTest {

    private final Sort defaultSort = Sort.by(Sort.Direction.ASC, "fireTime");

    @Test
    public void getSortWhenUnsortedShouldIgnoreDefault() {
        //Given
        var paging = new Paging();
        paging.setUnsorted(true);

        //When / Then
        assertEquals(Sort.unsorted(), paging.getSort(defaultSort));
    }

    @Test
    public void getSortWhenSortOrderIsCompleteShouldUseIt() {
        //Given
        var paging = pagingSortedBy(Sort.Direction.DESC, "name");

        //When / Then
        assertEquals(Sort.by(Sort.Direction.DESC, "name"), paging.getSort(defaultSort));
    }

    @Test
    public void getSortWhenSortOrderIsIncompleteShouldUseDefault() {
        //Given
        var missingProperty = pagingSortedBy(Sort.Direction.DESC, null);
        var missingDirection = pagingSortedBy(null, "name");

        //When / Then
        assertEquals(defaultSort, missingProperty.getSort(defaultSort));
        assertEquals(defaultSort, missingDirection.getSort(defaultSort));
    }

    @Test
    public void getSortWhenNoSortOrderOrDefaultShouldBeUnsorted() {
        assertEquals(Sort.unsorted(), new Paging().getSort(null));
    }

    @Test
    public void getSortWithSecondarySortShouldAppendItWhenSorted() {
        //Given
        var paging = pagingSortedBy(Sort.Direction.DESC, "name");
        var secondary = Sort.by("id");

        //When / Then
        assertEquals(Sort.by(Sort.Direction.DESC, "name").and(secondary), paging.getSort(defaultSort, secondary));
    }

    @Test
    public void getSortWithSecondarySortShouldNotAppendItWhenUnsorted() {
        //Given
        var paging = new Paging();
        paging.setUnsorted(true);

        //When / Then
        assertEquals(Sort.unsorted(), paging.getSort(defaultSort, Sort.by("id")));
        assertEquals(defaultSort, new Paging().getSort(defaultSort, null));
    }

    private static Paging pagingSortedBy(Sort.Direction direction, String property) {
        var sortOrder = new SortOrder();
        sortOrder.setDirection(direction);
        sortOrder.setProperty(property);
        var paging = new Paging();
        paging.setSortOrder(sortOrder);
        return paging;
    }
}
