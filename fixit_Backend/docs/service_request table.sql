CREATE TABLE service_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id UUID NOT NULL,
    provider_profile_id UUID NOT NULL,
    gig_id UUID NOT NULL,
    description TEXT NOT NULL,
    service_location VARCHAR(200) NOT NULL,
    scheduled_date DATE NOT NULL,
    proposed_price DECIMAL(12, 2) NOT NULL,
    agreed_price DECIMAL(12, 2),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_customer_profile
        FOREIGN KEY (customer_profile_id)
        REFERENCES profiles(id),
    CONSTRAINT fk_request_provider_profile
        FOREIGN KEY (provider_profile_id)
        REFERENCES profiles(id),
    CONSTRAINT fk_request_gig
        FOREIGN KEY (gig_id)
        REFERENCES gigs(id)
);