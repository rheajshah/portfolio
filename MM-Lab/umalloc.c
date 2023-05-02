#include "umalloc.h"
#include "csbrk.h"
#include <stdio.h>
#include <stdbool.h>
#include <assert.h>
#include "ansicolors.h"

const char author[] = ANSI_BOLD ANSI_COLOR_RED "Rhea Shah rjs4665" ANSI_RESET;

/*
 * The following helpers can be used to interact with the memory_block_t
 * struct, they can be adjusted as necessary.
 */

// A sample pointer to the start of the free list.
memory_block_t *free_head;

/*
 * is_allocated - returns true if a block is marked as allocated.
 */
bool is_allocated(memory_block_t *block) {
    assert(block != NULL);
    return block->block_size_alloc & 0x1;
}

/*
 * allocate - marks a block as allocated.
 */
void allocate(memory_block_t *block) {
    assert(block != NULL);
    block->block_size_alloc |= 0x1;
}


/*
 * deallocate - marks a block as unallocated.
 */
void deallocate(memory_block_t *block) {
    assert(block != NULL);
    block->block_size_alloc &= ~0x1;
}

/*
 * get_size - gets the size of the block.
 */
size_t get_size(memory_block_t *block) {
    assert(block != NULL);
    return block->block_size_alloc & ~(ALIGNMENT-1);
}

/*
 * get_next - gets the next block.
 */
memory_block_t *get_next(memory_block_t *block) {
    assert(block != NULL);
    return block->next;
}

/*
 * put_block - puts a block struct into memory at the specified address.
 * Initializes the size and allocated fields, along with NUlling out the next 
 * field.
 */
void put_block(memory_block_t *block, size_t size, bool alloc) {
    assert(block != NULL);
    assert(size % ALIGNMENT == 0);
    assert(alloc >> 1 == 0);
    block->block_size_alloc = size | alloc;
    block->next = NULL;
}

/*
 * get_payload - gets the payload of the block.
 */
void *get_payload(memory_block_t *block) {
    assert(block != NULL);
    return (void*)(block + 1);
}

/*
 * get_block - given a payload, returns the block.
 */
memory_block_t *get_block(void *payload) {
    assert(payload != NULL);
    return ((memory_block_t *)payload) - 1;
}

/*
 * The following are helper functions that can be implemented to assist in your
 * design, but they are not required. 
 */

/*
 * find - finds a free block (returns header) that can satisfy the umalloc request.
 */
memory_block_t *find(size_t size) {
    //* STUDENT TODO
    memory_block_t* nextAvailFreeBlock = free_head;
    while(nextAvailFreeBlock != NULL){
        if(nextAvailFreeBlock->block_size_alloc >= size){
            return nextAvailFreeBlock;
        }
        nextAvailFreeBlock = nextAvailFreeBlock -> next;
    }
    nextAvailFreeBlock = extend(size);
    return nextAvailFreeBlock;
}

int get_ceiling(int a, int b) {
    return ((a % b) != 0) + (a / b);
}

/*
 * extend - extends the heap if more memory is required. returns header
 */
memory_block_t *extend(size_t size) {
    //* STUDENT TODO

    size = PAGESIZE * get_ceiling(size, PAGESIZE);
    memory_block_t* currFreeBlock = free_head;
    if(currFreeBlock == NULL){
        free_head = csbrk(size);
        put_block(free_head, size, false);
        return free_head;
    }
    while(currFreeBlock -> next != NULL){
        currFreeBlock = currFreeBlock->next;
    }
    currFreeBlock->next = csbrk(size);
    put_block(currFreeBlock->next, size, false);
    return currFreeBlock->next;
}

/*
 * split - splits a given block in parts, one allocated, one free. return header.
 */
memory_block_t *split(memory_block_t *block, size_t size) { //size given is payload size
    //* STUDENT TODO
    memory_block_t *allocatedBlock = (memory_block_t*)((long)block + (block -> block_size_alloc - size)); //ending part of the split block
    put_block(allocatedBlock, size, true); 
    block -> block_size_alloc -= (size); 
    return allocatedBlock;
}

/*
 * coalesce - coalesces a free memory block with neighbors.
 */
memory_block_t *coalesce(memory_block_t *block) {
    //* STUDENT TODO
    return NULL;
}



/*
 * uinit - Used initialize metadata required to manage the heap
 * along with allocating initial memory.
 */
int uinit() {
    free_head = (memory_block_t*) csbrk(PAGESIZE);
    if(free_head == NULL){ //initialization didn't work
        return -1;
    }
    put_block(free_head, PAGESIZE - ALIGNMENT, false); //size = PAGESIZE - header
    return 0;
}

/*Returns the free block that comes right before another free block. If there is no previous free block, return null
Helper for umalloc()*/
memory_block_t *get_prev(memory_block_t *block) {
    memory_block_t *prevBlock = free_head;
    if(prevBlock == block){
        return NULL; //no prev exists
    }
    while(prevBlock -> next != block){
        prevBlock = prevBlock->next;
    }
    return prevBlock;
}

/*Returns the free block that is right before a given allocated block in memory. The passed in allocate block will be put back into the free list
Helper for ufree()*/
memory_block_t *get_prev_allocated(memory_block_t *block) {
    memory_block_t *currBlock = free_head;
    while(currBlock){
        if(currBlock > block){
            return NULL; //no prev exists
        } else if(!currBlock->next){ //at end of list
            return currBlock;
        } else if(currBlock->next > block){
            return currBlock;
        }
        currBlock = currBlock -> next;
    }
    return currBlock;
}

/*
 * umalloc -  allocates size bytes and returns a pointer to the allocated memory.
   returns a pointer to an allocated block payload of at least size
   bytes. The entire allocated block should lie within the heap region and should not overlap with any
   other allocated chunk.
 */
void *umalloc(size_t size) {
    //* STUDENT TODO
    size = ALIGN(size + ALIGNMENT);
    memory_block_t* blockToBeAllocated = find(size);
    bool splitRequired = blockToBeAllocated->block_size_alloc > size;
    if(splitRequired){
        blockToBeAllocated = split(blockToBeAllocated, size);
    } else{
        // 1. get prev node
            // case 1: it exists (great!) --> update the links in free list
            // case 2: it doesn't exist (rip) --> reset free_head
        memory_block_t* prevNode = get_prev(blockToBeAllocated);
        if(!prevNode){ //DNE
            free_head = free_head->next;
        } else {
            prevNode->next = blockToBeAllocated->next;
        }
        put_block(blockToBeAllocated, size, true);
    }
    return get_payload(blockToBeAllocated);
}

/*
 * ufree - frees the memory space pointed to by ptr, which must have been called
 * by a previous call to malloc.
 */
void ufree(void *ptr) {
    //* STUDENT TODO - reinsert free blocks in memory address order
    memory_block_t *blockToBeFreed = get_block(ptr);
    if(!is_allocated(blockToBeFreed)){
        return;
    }
    deallocate(blockToBeFreed);
    memory_block_t* prevNode = get_prev_allocated(blockToBeFreed);
    if(!prevNode){
        blockToBeFreed -> next = free_head;
        free_head = blockToBeFreed;
    } else{
        blockToBeFreed -> next = prevNode -> next; 
        prevNode -> next = blockToBeFreed;
    }
}