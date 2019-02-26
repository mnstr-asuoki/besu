/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.tests.acceptance.clique;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import org.junit.Test;

public class CliqueDiscardRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldDiscardVotes() throws IOException {
    final String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        pantheon.createCliqueNodeWithValidators("miner1", initialValidators);
    final PantheonNode minerNode2 =
        pantheon.createCliqueNodeWithValidators("miner2", initialValidators);
    final PantheonNode minerNode3 =
        pantheon.createCliqueNodeWithValidators("miner3", initialValidators);
    cluster.start(minerNode1, minerNode2, minerNode3);

    minerNode1.execute(cliqueTransactions.createRemoveProposal(minerNode2));
    minerNode2.execute(cliqueTransactions.createRemoveProposal(minerNode2));
    minerNode1.execute(cliqueTransactions.createAddProposal(minerNode3));
    minerNode2.execute(cliqueTransactions.createAddProposal(minerNode3));
    minerNode1.execute(cliqueTransactions.createDiscardProposal(minerNode2));
    minerNode1.execute(cliqueTransactions.createDiscardProposal(minerNode3));

    minerNode1.waitUntil(wait.chainHeadHasProgressedByAtLeast(minerNode1, 2));

    cluster.verify(clique.validatorsEqual(minerNode1, minerNode2));
    minerNode1.verify(clique.noProposals());
    minerNode2.verify(
        clique.proposalsEqual().removeProposal(minerNode2).addProposal(minerNode3).build());
  }
}
