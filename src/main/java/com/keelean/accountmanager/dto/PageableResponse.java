package com.keelean.accountmanager.dto;

import org.springframework.data.domain.Page;

import java.util.Collection;

public class PageableResponse<T> extends BaseRestResponse {
    private static final long serialVersionUID = -2846910247972036035L;
    private int totalElements;
    private int totalPages;
    private int size;
    private int numberOfElements;
    private int number;
    private boolean last;
    private boolean first;
    private boolean sorted;
    private boolean empty;
    private Collection<T> content;

    public PageableResponse() {
    }

    public PageableResponse(Page<T> page, Collection<T> content) {
        this.totalElements = (int)page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.numberOfElements = (int)page.getTotalElements();
        this.number = page.getNumber();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.sorted = page.getSort().isSorted();
        this.empty = page.isEmpty();
        this.content = content;
    }

    public int getTotalElements() {
        return this.totalElements;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public int getSize() {
        return this.size;
    }

    public int getNumberOfElements() {
        return this.numberOfElements;
    }

    public int getNumber() {
        return this.number;
    }

    public boolean isLast() {
        return this.last;
    }

    public boolean isFirst() {
        return this.first;
    }

    public boolean isSorted() {
        return this.sorted;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public Collection<T> getContent() {
        return this.content;
    }

    public void setTotalElements(final int totalElements) {
        this.totalElements = totalElements;
    }

    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public void setNumberOfElements(final int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public void setLast(final boolean last) {
        this.last = last;
    }

    public void setFirst(final boolean first) {
        this.first = first;
    }

    public void setSorted(final boolean sorted) {
        this.sorted = sorted;
    }

    public void setEmpty(final boolean empty) {
        this.empty = empty;
    }

    public void setContent(final Collection<T> content) {
        this.content = content;
    }

    public String toString() {
        int var10000 = this.getTotalElements();
        return "PageableResponse(totalElements=" + var10000 + ", totalPages=" + this.getTotalPages() + ", size=" + this.getSize() + ", numberOfElements=" + this.getNumberOfElements() + ", number=" + this.getNumber() + ", last=" + this.isLast() + ", first=" + this.isFirst() + ", sorted=" + this.isSorted() + ", empty=" + this.isEmpty() + ", content=" + this.getContent() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageableResponse)) {
            return false;
        } else {
            PageableResponse<?> other = (PageableResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else if (this.getTotalElements() != other.getTotalElements()) {
                return false;
            } else if (this.getTotalPages() != other.getTotalPages()) {
                return false;
            } else if (this.getSize() != other.getSize()) {
                return false;
            } else if (this.getNumberOfElements() != other.getNumberOfElements()) {
                return false;
            } else if (this.getNumber() != other.getNumber()) {
                return false;
            } else if (this.isLast() != other.isLast()) {
                return false;
            } else if (this.isFirst() != other.isFirst()) {
                return false;
            } else if (this.isSorted() != other.isSorted()) {
                return false;
            } else if (this.isEmpty() != other.isEmpty()) {
                return false;
            } else {
                Object this$content = this.getContent();
                Object other$content = other.getContent();
                if (this$content == null) {
                    if (other$content != null) {
                        return false;
                    }
                } else if (!this$content.equals(other$content)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PageableResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        result = result * 59 + this.getTotalElements();
        result = result * 59 + this.getTotalPages();
        result = result * 59 + this.getSize();
        result = result * 59 + this.getNumberOfElements();
        result = result * 59 + this.getNumber();
        result = result * 59 + (this.isLast() ? 79 : 97);
        result = result * 59 + (this.isFirst() ? 79 : 97);
        result = result * 59 + (this.isSorted() ? 79 : 97);
        result = result * 59 + (this.isEmpty() ? 79 : 97);
        Object $content = this.getContent();
        result = result * 59 + ($content == null ? 43 : $content.hashCode());
        return result;
    }
}
