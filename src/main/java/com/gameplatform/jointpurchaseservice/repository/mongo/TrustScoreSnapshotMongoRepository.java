package com.gameplatform.jointpurchaseservice.repository.mongo;

import com.gameplatform.jointpurchaseservice.domain.document.TrustScoreSnapshotDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrustScoreSnapshotMongoRepository extends MongoRepository<TrustScoreSnapshotDocument, String> {
}