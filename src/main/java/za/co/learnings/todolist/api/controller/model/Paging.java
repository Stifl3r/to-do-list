package za.co.learnings.todolist.api.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class Paging implements Serializable {

    private static final long serialVersionUID = 8125063931017487093L;

    @Schema(required = true, example = "0")
    private Integer pageIndex;
    @Schema(required = true, example = "10")
    private Integer pageSize;
    private SortOrder sortOrder;
    private boolean unsorted = false;

    /**
     * Get a sort object from the specified sort properties, else default
     *
     * @param defaultSort if no sort exists, default
     * @return Return a Sort object
     */
    public Sort getSort(Sort defaultSort) {
        Sort sort;

        if (this.isUnsorted()) {
            sort = Sort.unsorted();
        } else {
            SortOrder order = this.getSortOrder();
            if (order != null && order.getDirection() != null && order.getProperty() != null) {
                sort = Sort.by(order.getDirection(), order.getProperty());
            } else {
                sort = Objects.requireNonNullElseGet(defaultSort, Sort::unsorted);
            }
        }

        return sort;
    }

    /**
     * Get a sort object from the specified sort properties, else default. Also a secondary sort column is added to the sort.
     *
     * @param defaultSort      if no sort exists, default
     * @param alwaysSecondSort secondary sort column to order by always unless unsorted
     * @return Return a Sort object
     */
    public Sort getSort(Sort defaultSort, Sort alwaysSecondSort) {
        Sort sort = this.getSort(defaultSort);

        if (alwaysSecondSort != null && alwaysSecondSort.isSorted() && sort.isSorted()) {
            sort = sort.and(alwaysSecondSort);
        }

        return sort;
    }
}
