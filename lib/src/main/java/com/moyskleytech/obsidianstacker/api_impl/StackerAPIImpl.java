package com.moyskleytech.obsidianstacker.api_impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import java.util.Optional;
import com.moyskleytech.obsidianstacker.api.Stack;
import com.moyskleytech.obsidianstacker.api.StackerAPI;
import com.moyskleytech.obsidianstacker.utils.Scheduler;

public class StackerAPIImpl extends StackerAPI {

    public StackerAPIImpl(Plugin p) {
        super(p);
    }

    @Override
    public CompletableFuture<List<Stack>> getStacks(Chunk c) {
        CompletableFuture<List<Stack>> answer = new CompletableFuture<>();

        Scheduler.getInstance().runChunkTask(getPlugin(), c, 0, ()->{
            List<Stack> answerList =Arrays.stream(c.getEntities()).filter((e)->isStack(e)).map(x->new StackImpl(x)).collect(Collectors.toList());
            answer.complete(answerList);
        });
        return answer;
    }

    @Override
    public CompletableFuture<List<Stack>> getStacks(World w) {
        long count= w.getChunkCount();
        List<Stack> answerList = new ArrayList<>();
        CompletableFuture<List<Stack>> answer = new CompletableFuture<>();
        AtomicLong remainingCount = new AtomicLong();
        remainingCount.set(count);

        for(long i=0;i<count;i++)
        {
            getStacks(w.getChunkAt(i)).thenAccept(x->{
                answerList.addAll(answerList);
                if(remainingCount.decrementAndGet()==0)
                {
                    answer.complete(answerList);
                }
            });
        }
        return answer;
    }

    @Override
    public java.util.Optional<Stack> getStack(Block b) {
        Optional<Entity> entity = b.getWorld().getNearbyEntities(
        b.getLocation().clone().add(0.5,1,0.5),0.5,0.5,0.5).stream().filter((e)->isStack(e)).findAny();
        return entity.map(x->new StackImpl(x));
    }
    
}
