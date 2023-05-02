
#include "umalloc.h"
#include <stdio.h>

//Place any variables needed here from umalloc.c as an extern.
extern memory_block_t *free_head;

/*
 * check_heap -  used to check that the heap is still in a consistent state.
 *
 * STUDENT TODO:
 * Required to be completed for checkpoint 1:
 *
 *      - Ensure that the free block list is in the order you expect it to be in.
 *        As an example, if you maintain the free list in memory-address order,
 *        lowest address first, ensure that memory addresses strictly ascend as you
 *        traverse the free list.
 *
 *      - Check if any free memory_blocks overlap with each other. 
 *
 *      - Ensure that each memory_block is aligned. 
 * 
 * Should return 0 if the heap is still consistent, otherwise return a non-zero
 * return code. Asserts are also a useful tool here.
 */
int check_heap() {
    // Example heap check:
    // Check that all blocks in the free list are marked free.
    // If a block is marked allocated, return -1.
    
    memory_block_t *cur = free_head;
    while (cur) {
        if (is_allocated(cur)) {
            printf("is_allocated() == false\n");
            return -1;
        }
        cur = cur -> next;
    }

    /** Ensure that the free block list is in the order you expect it to be in.
     * As an example, if you maintain the free list in memory-address order,
     * lowest address first, ensure that memory addresses strictly ascend as you
     * traverse the free list.
     * 
     * Check if any free memory_blocks overlap with each other 
     * 
     * (because blocks are story in memory order, both checks are satisfied here) **/
    cur = free_head;
    while(cur -> next != NULL){
        if(cur >= cur -> next){
            printf("memory blocks overlap with each other\n");
            return -1;
        }
        cur = cur -> next;
    }

    /** Ensure that each memory_block is aligned */
    cur = free_head;
    while(cur){
        if((long)cur % ALIGNMENT != 0){
            printf("memory blocks aren't aligned\n");
            return -1;
        }
        cur = cur -> next;
    }
    
    /** Ensure that each free memory_block has no overlap*/
    cur = free_head;
    while(cur -> next != NULL){
        if(((long)cur + get_size(cur)) > (long)(cur -> next)){
            printf("cur: %p\n", cur);
            printf("cur size: %d\n", (int) cur->block_size_alloc);
            printf("cur->next: %p\n", cur->next);
            printf("memory blocks overlap with each other\n");
            return -1;
        }
        printf("size of cur free block: %ld \n", get_size(cur));
        printf("size of cur mem address: %p \n", cur);
        printf("size of cur next mem address: %p \n", cur->next);
        cur = cur -> next;
    }

    return 0;
}