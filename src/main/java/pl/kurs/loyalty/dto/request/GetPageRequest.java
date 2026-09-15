package pl.kurs.loyalty.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record GetPageRequest(
        Integer page,
        Integer size
) {
    public Pageable toPageable() {
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 0;

        return PageRequest.of(pageNumber, pageSize);
    }
}
