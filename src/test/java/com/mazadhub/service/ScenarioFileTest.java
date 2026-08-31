package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

// Data-driven tests: every .txt file under src/test/resources/scenarios is a small auction script
// Each file is replayed against the real services and every EXPECT line is checked
class ScenarioFileTest
{
    private static final Instant NOW=Instant.parse("2026-06-11T12:00:00Z");

    // Turns each scenario file into its own test, so a failure names the file that broke
    @TestFactory
    Stream<DynamicTest> everyScenarioFile() throws Exception
    {
        Path dir=resourceDir("scenarios");
        try(var files=Files.list(dir))
        {
            return files.filter(p->p.toString().endsWith(".txt"))
                    .sorted()
                    .map(p->DynamicTest.dynamicTest(p.getFileName().toString(), ()->runScript(p)))
                    .toList()
                    .stream();
        }
    }

    // Finds the scenarios folder on the test classpath
    private Path resourceDir(String name) throws URISyntaxException
    {
        URL url=getClass().getClassLoader().getResource(name);
        if(url==null)
        {
            throw new IllegalStateException("Missing test resources folder: "+name);
        }

        return Path.of(url.toURI());
    }

    // Replays one file line by line: START, BID / AUTO, REJECT and EXPECT
    private void runScript(Path file) throws IOException
    {
        List<String> lines=Files.readAllLines(file);

        Fakes.Users users=new Fakes.Users();
        Fakes.Items items=new Fakes.Items();
        Fakes.Bids bids=new Fakes.Bids();
        Fakes.AutoBids autoBids=new Fakes.AutoBids();
        Fakes.Notifier notifier=new Fakes.Notifier();

        BiddingService service=new BiddingService(items, bids, autoBids, users, notifier)
        {
            @Override
            protected Instant now()
            {
                return NOW;
            }
        };

        User seller=users.save(new User("seller", "h", UserRole.USER));
        Category category=TestIds.withId(new Category("Test", "d"), 1L);
        Map<String, User> people=new HashMap<>();
        Item item=null;
        List<String> trace=new ArrayList<>();

        for(int i=0; i<lines.size(); i++)
        {
            String line=lines.get(i).trim();
            if(line.isEmpty()||line.startsWith("#"))
            {
                continue;
            }

            String[] parts=line.split("\\s+");
            int lineNo=i+1;

            switch(parts[0].toUpperCase())
            {
                case "START"->
                {
                    item=items.save(new Item(seller, category, "Item",
                            new BigDecimal(parts[1]), NOW.plusSeconds(86400)));
                    trace.add("START "+parts[1]);
                }

                case "BID", "AUTO"->
                {
                    Item current=require(item, lineNo);
                    User actor=people.computeIfAbsent(parts[1],
                            n->users.save(new User(n, "h", UserRole.USER)));
                    BigDecimal amount=new BigDecimal(parts[2]);
                    try
                    {
                        if(parts[0].equalsIgnoreCase("AUTO"))
                        {
                            service.placeAutoBid(current.getId(), actor.getId(), amount);
                        }
                        else
                        {
                            service.placeBid(current.getId(), actor.getId(), amount);
                        }

                        trace.add(line);
                    }
                    catch(RuntimeException e)
                    {
                        fail("line "+lineNo+": "+line+" was unexpectedly refused ("
                                +e.getClass().getSimpleName()+": "+e.getMessage()+")\ntrace:\n"
                                +String.join("\n", trace));
                    }
                }

                case "REJECT"->
                {
                    Item current=require(item, lineNo);
                    User actor=people.computeIfAbsent(parts[1],
                            n->users.save(new User(n, "h", UserRole.USER)));
                    BigDecimal amount=new BigDecimal(parts[2]);
                    boolean refused=false;
                    try
                    {
                        service.placeBid(current.getId(), actor.getId(), amount);
                    }
                    catch(RuntimeException expected)
                    {
                        refused=true;
                    }

                    if(!refused)
                    {
                        fail("line "+lineNo+": "+line+" should have been refused but was accepted");
                    }

                    trace.add(line);
                }

                case "EXPECT"->
                {
                    Item current=require(item, lineNo);
                    BigDecimal expectedPrice=new BigDecimal(parts[1]);
                    String expectedLeader=parts[2];
                    String actualLeader=current.getWinner()==null
                            ?"none":current.getWinner().getUsername();
                    String where=file.getFileName()+" line "+lineNo+"\ntrace:\n"
                            +String.join("\n", trace);
                    assertEquals(0, current.getCurrentPrice().compareTo(expectedPrice),
                            "price mismatch at "+where+"\nexpected "+expectedPrice
                                    +" but was "+current.getCurrentPrice());
                    assertEquals(expectedLeader, actualLeader, "leader mismatch at "+where);
                    trace.add(line+"  [ok]");
                }

                default->fail("line "+lineNo+": unknown command '"+parts[0]+"'");
            }
        }
    }

    private Item require(Item item, int lineNo)
    {
        if(item==null)
        {
            fail("line "+lineNo+": the script must begin with START <price>");
        }

        return item;
    }
}
